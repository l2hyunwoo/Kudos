"""Import the exact Android artifacts and dependency edges selected by Gradle."""

def _label(coordinate):
    return "_".join(coordinate.split(":")[:2]).replace(".", "_").replace("-", "_")

def _android_dependencies_impl(ctx):
    artifacts = json.decode(ctx.read(ctx.attr.manifest))
    lines = [
        'load("@rules_android//rules:rules.bzl", "aar_import", "android_library")',
        'load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_import")',
        'package(default_visibility = ["//visibility:public"])',
    ]
    names = {}
    types = {a["coordinate"]: a["coordinate"].split(":")[2] for a in artifacts}
    downloads = []
    for artifact in artifacts:
        coordinate = artifact["coordinate"]
        name = _label(coordinate)
        if name in names:
            fail("Duplicate Android artifact label: " + name)
        names[name] = True
        extension = coordinate.split(":")[2]
        if extension not in ["aar", "jar"]:
            fail("Unsupported Android artifact: " + coordinate)
        path = "artifacts/" + name + "." + extension
        downloads.append(ctx.download(url = artifact["url"], output = path, sha256 = artifact["sha256"], canonical_id = artifact["url"], block = False))
        deps = [":" + _label(dep) for dep in artifact["dependencies"]]
        rule = "aar_import" if extension == "aar" else "kt_jvm_import"
        if extension == "jar" and any([types[d] == "aar" for d in artifact["dependencies"]]):
            # JavaInfo alone does not propagate Android manifests/resources across a JAR-to-AAR edge.
            lines.append("kt_jvm_import(name = %r, jar = %r, deps = %r)" % (name + "_jar", path, deps))
            lines.append("android_library(name = %r, exports = %r)" % (name, [":" + name + "_jar"] + deps))
        else:
            lines.append("%s(name = %r, %s = %r, deps = %r)" % (rule, name, extension, path, deps))
    for download in downloads:
        download.wait()
    ctx.file("BUILD.bazel", "\n".join(lines) + "\n")

android_dependencies = repository_rule(
    implementation = _android_dependencies_impl,
    attrs = {"manifest": attr.label(mandatory = True, allow_single_file = [".json"])},
)
