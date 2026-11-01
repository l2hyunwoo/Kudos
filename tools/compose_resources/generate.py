"""Port of the Compose Multiplatform 1.11.1 Gradle resource generator (Res class, accessors, .cvr).

Mirrors org.jetbrains.compose.resources.{GeneratedResClassSpec,GenerateResourceAccessorsTask,
XmlValuesConverterTask} so Bazel builds get the same `Res.*` API and byte-identical .cvr files.
Collectors are emitted without `expect`/`actual` because Bazel compiles common+android as one unit
(the Gradle plugin does the same for JVM-only projects via useActualModifier=false).
"""
import argparse
import base64
import os
import re
import sys
import zipfile
import xml.dom.minidom

FORMAT_VERSION = 0
ITEMS_PER_FILE_LIMIT = 100
# (typeName, accessorName, className); order = ResourceType enum order.
TYPES = [
    ("drawable", "drawable", "DrawableResource"),
    ("string", "string", "StringResource"),
    ("string-array", "array", "StringArrayResource"),
    ("plurals", "plurals", "PluralStringResource"),
    ("font", "font", "FontResource"),
]
TYPE_INDEX = {t[0]: i for i, t in enumerate(TYPES)}
KEYED = {"string", "string-array", "plurals"}


def java_int(v):
    v &= 0xFFFFFFFF
    return v - (1 << 32) if v >= 1 << 31 else v


def bytes_hash(data):  # java.util.Arrays.hashCode(byte[])
    h = 1
    for b in data:
        h = java_int(31 * h + (b - 256 if b > 127 else b))
    return h


def string_hash(s):  # java.lang.String.hashCode (UTF-16 code units)
    h = 0
    units = s.encode("utf-16-be")
    for i in range(0, len(units), 2):
        h = java_int(31 * h + ((units[i] << 8) | units[i + 1]))
    return h


def kotlin_int_literal(v):  # KotlinPoet %L of Int: underscores every 3 digits
    s = f"{abs(v):,}".replace(",", "_")
    return ("-" if v < 0 else "") + s


def handle_special_characters(s):
    double_slash = {m.start() for m in re.finditer(r"\\\\", s)}

    def repl(m):
        if m.start() - 1 in double_slash:
            return m.group(0)
        v = m.group(0)
        if v == "\\n":
            return "\n"
        if v == "\\t":
            return "\t"
        return chr(int(v[2:], 16))

    return re.sub(r"\\u[a-fA-F\d]{4}|\\n|\\t", repl, s).replace("\\\\", "\\")


def b64(s):
    return base64.b64encode(s.encode("utf-8")).decode("ascii")


def text_content(node):
    out = []
    for c in node.childNodes:
        if c.nodeType in (c.TEXT_NODE, c.CDATA_SECTION_NODE):
            out.append(c.data)
        elif c.nodeType == c.ELEMENT_NODE:
            out.append(text_content(c))
    return "".join(out)


def convert_values_xml(path):
    doc = xml.dom.minidom.parse(path)
    records = []
    seen = {}
    for node in doc.getElementsByTagName("resources")[0].childNodes:
        if node.nodeType != node.ELEMENT_NODE or not node.attributes.length:
            continue
        tag = node.nodeName
        if tag.lower() not in TYPE_INDEX or tag.lower() in ("drawable", "font"):
            sys.exit(f"Unknown string resource type: '{tag}' in {path}")
        key = node.getAttribute("name")
        if tag == "string":
            value = b64(handle_special_characters(text_content(node)))
        else:
            items = [c for c in node.childNodes if c.nodeType == c.ELEMENT_NODE and c.nodeName == "item"]
            if tag == "string-array":
                value = ",".join(b64(handle_special_characters(text_content(c))) for c in items)
            else:
                value = ",".join(
                    c.getAttribute("quantity").upper() + ":" + b64(handle_special_characters(text_content(c)))
                    for c in items
                )
        if (key, tag) in seen:
            sys.exit(f"Duplicated key '{key}' in {path}")
        seen[(key, tag)] = True
        records.append("|".join([tag, key, value]))
    return f"version:{FORMAT_VERSION}\n" + "".join(r + "\n" for r in sorted(records))


def identifier(name):
    name = name.replace("-", "_")
    return "_" + name if name and name[0].isdigit() else name


def qualifier_code(qualifiers, path):
    found = {}
    for q in qualifiers:
        if q in ("light", "dark"):
            kind = "theme"
        elif q in ("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi", "ldpi"):
            kind = "density"
        elif re.fullmatch(r"[a-z]{2,3}", q):
            kind = "language"
        elif re.fullmatch(r"r[A-Z]{2}", q):
            kind = "region"
        else:
            sys.exit(f"{path} contains unknown qualifier: '{q}'.")
        if kind in found:
            sys.exit(f"{path} contains repetitive qualifiers: '{found[kind]}' and '{q}'.")
        found[kind] = q
    code, imports = "", set()
    if "theme" in found:
        code += f"ThemeQualifier.{found['theme'].upper()}, "
        imports.add("ThemeQualifier")
    if "density" in found:
        code += f"DensityQualifier.{found['density'].upper()}, "
        imports.add("DensityQualifier")
    if "language" in found:
        code += f'LanguageQualifier("{found["language"]}"), '
        imports.add("LanguageQualifier")
    if "region" in found:
        if "language" not in found:
            sys.exit(f"Region qualifier must be used only with language.\nFile: {path}")
        code += f'RegionQualifier("{found["region"][-2:]}"), '
        imports.add("RegionQualifier")
    return code, imports


def collect_items(res_dir, cvr_texts, source_set):
    """type -> name -> [item]; item = (qualifiers, relpath, hash, offset, size)."""
    resources = {}
    for d in sorted(os.listdir(res_dir)):
        full = os.path.join(res_dir, d)
        if d.startswith("."):
            continue
        if not os.path.isdir(full):
            sys.exit(f"{d} is not directory! Raw files should be placed in '{os.path.basename(res_dir)}/files' directory.")
        parts = d.split("-")
        type_name, qualifiers = parts[0].lower(), parts[1:]
        if type_name == "string":
            sys.exit(f"Forbidden directory name '{d}'! String resources should be declared in 'values/strings.xml'.")
        if type_name == "files":
            if qualifiers:
                sys.exit(f"The 'files' directory doesn't support qualifiers: '{d}'.")
            continue
        for f in sorted(os.listdir(full)):
            if f.startswith("."):
                continue
            if type_name == "values":
                if not f.lower().endswith(".xml"):
                    continue
                rel = f"{d}/{f[:-4]}.{source_set}.cvr"
                offset = 0
                for line in cvr_texts[rel].split("\n")[:-1]:
                    size = len(line.encode("utf-8"))
                    if offset > 0:
                        t, key, content = line.split("|")[:3]
                        item = (qualifiers, rel, string_hash(content), offset, size)
                        resources.setdefault(t, {}).setdefault(identifier(key), []).append(item)
                    offset += size + 1
                continue
            if type_name not in TYPE_INDEX:
                sys.exit(f"Unknown resource type: '{type_name}'.")
            with open(os.path.join(full, f), "rb") as fh:
                h = bytes_hash(fh.read())
            name = identifier(os.path.splitext(f)[0])
            resources.setdefault(type_name, {}).setdefault(name, []).append((qualifiers, f"{d}/{f}", h, -1, -1))
    return resources


def res_kt(pkg, module_dir, vis):
    objs = "\n\n".join(f"  public object {t[1]}" for t in TYPES)
    return f'''@file:OptIn(InternalResourceApi::class)
@file:Suppress(
  "RedundantVisibilityModifier",
  "REDUNDANT_VISIBILITY_MODIFIER",
)

package {pkg}

import kotlin.ByteArray
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.getResourceUri
import org.jetbrains.compose.resources.readResourceBytes

{vis} object Res {{
  /**
   * Reads the content of the resource file at the specified path and returns it as a byte array.
   *
   * Example: `val bytes = Res.readBytes("files/key.bin")`
   *
   * @param path The path of the file to read in the compose resource's directory.
   * @return The content of the file as a byte array.
   */
  public suspend fun readBytes(path: String): ByteArray = readResourceBytes("{module_dir}" + path)

  /**
   * Returns the URI string of the resource file at the specified path.
   *
   * Example: `val uri = Res.getUri("files/key.bin")`
   *
   * @param path The path of the file in the compose resource's directory.
   * @return The URI string of the file.
   */
  public fun getUri(path: String): String = getResourceUri("{module_dir}" + path)

{objs}
}}
'''


def chunk_kt(pkg, module_dir, vis, type_name, chunk_class, entries):
    accessor, cls = TYPES[TYPE_INDEX[type_name]][1:]
    imports = {"InternalResourceApi", "ResourceItem", cls}
    props = []
    for name, items in entries:
        lines = []
        for qualifiers, rel, _, offset, size in items:
            q, qi = qualifier_code(qualifiers, rel)
            imports |= qi
            lines.append(f'        ResourceItem(setOf({q}), "${{MD}}{rel}", {offset}, {size}),\n')
        content_hash = 0
        for item in items:
            content_hash = java_int(content_hash * 31 + item[2])
        head = f'{cls}("{type_name}:{name}", "{name}", setOf(\n' if type_name in KEYED else f'{cls}("{type_name}:{name}", setOf(\n'
        props.append(
            f"@delegate:ResourceContentHash({kotlin_int_literal(content_hash)})\n"
            f"{vis} val Res.{accessor}.{name}: {cls} by lazy {{\n"
            f"      {head}{''.join(lines)}      ))\n"
            f"    }}\n"
        )
    imports.add("ResourceContentHash")
    kotlin_imports = ["kotlin.OptIn", "kotlin.String", "kotlin.collections.MutableMap"]
    all_imports = sorted(kotlin_imports + [f"org.jetbrains.compose.resources.{i}" for i in imports])
    puts = "".join(f'  map.put("{n}", Res.{accessor}.{n})\n' for n, _ in entries)
    return (
        "@file:OptIn(InternalResourceApi::class)\n\n"
        f"package {pkg}\n\n"
        + "".join(f"import {i}\n" for i in all_imports)
        + f'\nprivate const val MD: String = "{module_dir}"\n\n'
        + "\n".join(props)
        + f"\n@InternalResourceApi\ninternal fun _collect{chunk_class}Resources(map: MutableMap<String, {cls}>) {{\n{puts}}}\n"
    )


def collectors_kt(pkg, vis, collectors):
    imports = "".join(f"import org.jetbrains.compose.resources.{c}\n" for c in sorted(t[2] for t in TYPES))
    body = []
    for _, _, cls in TYPES:
        calls = "".join(f"  {fn}(map)\n" for fn in collectors.get(cls, []))
        body.append(
            f"{vis} val Res.all{cls}s: Map<String, {cls}> by lazy {{\n"
            f"  val map = mutableMapOf<String, {cls}>()\n{calls}  return@lazy map\n}}\n"
        )
    return (
        "@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)\n\n"
        f"package {pkg}\n\nimport kotlin.OptIn\nimport kotlin.String\nimport kotlin.collections.Map\n"
        + imports + "\n" + "\n".join(body)
    )


def generate(res_dir, pkg, source_set="commonMain", public=False):
    """Returns (kotlin files {name: text}, assets {relpath: bytes}) relative to composeResources/<pkg>/."""
    vis = "public" if public else "internal"
    module_dir = f"composeResources/{pkg}/"
    assets, cvr_texts = {}, {}
    for d in sorted(os.listdir(res_dir)):
        full = os.path.join(res_dir, d)
        if d.startswith(".") or not os.path.isdir(full):
            continue
        for f in sorted(os.listdir(full)):
            if f.startswith("."):
                continue
            if d.startswith("values"):
                if f.lower().endswith(".xml"):
                    rel = f"{d}/{f[:-4]}.{source_set}.cvr"
                    cvr_texts[rel] = convert_values_xml(os.path.join(full, f))
                    assets[rel] = cvr_texts[rel].encode("utf-8")
            else:
                with open(os.path.join(full, f), "rb") as fh:
                    assets[f"{d}/{f}"] = fh.read()
    resources = collect_items(res_dir, cvr_texts, source_set)
    files = {"Res.kt": res_kt(pkg, module_dir, vis)}
    collectors = {}
    ss_cap = source_set[0].upper() + source_set[1:]
    for type_name, _, cls in sorted(TYPES, key=lambda t: TYPE_INDEX[t[0]]):
        by_name = resources.get(type_name)
        if not by_name:
            continue
        # TreeMap<String> sorts by UTF-16 code units; identifiers are ASCII so Python sort matches.
        names = sorted(by_name)
        accessor = TYPES[TYPE_INDEX[type_name]][1]
        acc_cap = accessor[0].upper() + accessor[1:]
        for idx in range(0, len(names), ITEMS_PER_FILE_LIMIT):
            chunk = names[idx : idx + ITEMS_PER_FILE_LIMIT]
            n = idx // ITEMS_PER_FILE_LIMIT
            chunk_class = f"{ss_cap}{acc_cap}{n}"
            entries = [(name, sorted(by_name[name], key=lambda it: it[1])) for name in chunk]
            files[f"{acc_cap}{n}.{source_set}.kt"] = chunk_kt(pkg, module_dir, vis, type_name, chunk_class, entries)
            collectors.setdefault(cls, []).append(f"_collect{chunk_class}Resources")
    files["ResourceCollectors.kt"] = collectors_kt(pkg, vis, collectors)
    return files, assets


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--res_dir", required=True)
    p.add_argument("--package", required=True)
    p.add_argument("--srcjar", required=True)
    p.add_argument("--assets_root", required=True, help="directory that receives composeResources/<pkg>/...")
    p.add_argument("--public", action="store_true")
    a = p.parse_args()
    files, assets = generate(a.res_dir, a.package, public=a.public)
    pkg_path = a.package.replace(".", "/")
    with zipfile.ZipFile(a.srcjar, "w") as z:
        for name in sorted(files):
            info = zipfile.ZipInfo(f"{pkg_path}/{name}", date_time=(1980, 1, 1, 0, 0, 0))
            z.writestr(info, files[name])
    for rel, data in assets.items():
        out = os.path.join(a.assets_root, "composeResources", a.package, rel)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, "wb") as fh:
            fh.write(data)


if __name__ == "__main__":
    main()
