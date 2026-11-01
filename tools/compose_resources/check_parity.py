"""Compare the port with existing Compose Gradle task outputs, including resource bytes."""
from pathlib import Path
import sys
from generate import generate

root = Path(__file__).resolve().parents[2]
modules = sys.argv[1:] or ['core/design', 'core/soil', 'feature/category', 'feature/main', 'feature/tasks', 'shared']
for module in modules:
    base = root / module
    generated = base / 'build/generated/compose/resourceGenerator'
    files, assets = generate(str(base / 'src/commonMain/composeResources'), 'kudos.' + module.replace('/', '.') + '.generated.resources')
    expected = {}
    for directory in ['commonResClass', 'commonMainResourceAccessors', 'androidMainResourceCollectors']:
        paths = list((generated / 'kotlin' / directory).rglob('*.kt'))
        assert paths, f'{module}: run Gradle resource generation first ({directory})'
        for path in paths:
            name = path.name
            content = path.read_text()
            if name == 'ActualResourceCollectors.kt':
                name = 'ResourceCollectors.kt'
                content = content.replace(' actual val ', ' val ')
            expected[name] = content
    assert files.keys() == expected.keys(), (module, files.keys(), expected.keys())
    for name in files:
        assert files[name] == expected[name], f'{module}: Kotlin differs: {name}'
    prepared = generated / 'preparedResources/commonMain/composeResources'
    expected_assets = {str(p.relative_to(prepared)): p.read_bytes() for p in prepared.rglob('*') if p.is_file()}
    assert assets == expected_assets, f'{module}: resource bytes differ'
    print(f'{module}: OK ({len(files)} Kotlin files, {len(assets)} assets)')
