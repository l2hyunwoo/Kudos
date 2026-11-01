"""Check Bazel coordinates against the Android export and the Gradle version catalog."""
import json
from pathlib import Path
import re
import tomllib

root = Path(__file__).resolve().parents[1]
module = (root / 'MODULE.bazel').read_text()
coordinates = re.findall(r'"([^"\s]+:[^"\s]+:[^"\s]+)"', module)
versions = {':'.join(c.split(':')[:2]): c.split(':')[-1] for c in coordinates}
exported = json.loads((root / 'tools/android-dependencies.json').read_text())
for coordinate in exported:
    assert coordinate in coordinates, f'MODULE.bazel differs from Android export: {coordinate}'
catalog = tomllib.loads((root / 'gradle/libs.versions.toml').read_text())
checked = set()
for alias, library in catalog['libraries'].items():
    name = library.get('module') or library['group'] + ':' + library['name']
    # These catalog entries are unused by the Android build; transitive versions may differ.
    if alias in {'androidx-core-ktx', 'androidx-appcompat', 'androidx-testExt-junit', 'androidx-espresso-core'}:
        continue
    if name not in versions:
        continue
    version = library['version']
    expected = catalog['versions'][version['ref']] if isinstance(version, dict) else version
    assert versions[name] == expected, f'{alias}: Bazel={versions[name]}, Gradle={expected}'
    checked.add(alias)
for compiler in ['kotlin', 'ksp']:
    expected = catalog['versions'][compiler]
    tag = 'kotlinc_version' if compiler == 'kotlin' else 'ksp_version'
    assert re.search(r'\.' + tag + r'\(\s*version = "' + re.escape(expected) + '"', module), compiler
assert versions['dev.zacsweers.metro:compiler'] == catalog['versions']['metro']
assert versions['org.jetbrains.kotlin:kotlin-compose-compiler-plugin-embeddable'] == catalog['versions']['kotlin']
assert versions['org.jetbrains.kotlin:kotlin-serialization-compiler-plugin-embeddable'] == catalog['versions']['kotlin']
assert versions['de.jensklingenberg.ktorfit:ktorfit-ksp'] == catalog['versions']['ktorfit']
for coordinate in ['com.squareup.okhttp3:logging-interceptor', 'org.slf4j:slf4j-android']:
    source = root / ('core/network/build.gradle.kts' if 'okhttp3' in coordinate else 'build-logic/src/main/kotlin/io/github/l2hyunwoo/kudos/primitive/KtorPlugin.kt')
    assert coordinate + ':' + versions[coordinate] in source.read_text(), coordinate
print(f'OK: {len(exported)} Android coordinates, {len(checked)} catalog libraries, compiler and processor pins')
