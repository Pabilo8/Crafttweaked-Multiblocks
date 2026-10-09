#!/usr/bin/env python3
"""Static AMT/Deco contracts and numeric geometry oracles; no Java or game execution."""
import argparse
import json
import math
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/pl/pabilo8/ctmb'


def read(path):
    return (JAVA / path).read_text()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('ii', type=Path, help='Released II 0.3.1 source checkout')
    args = parser.parse_args()
    ii = args.ii / 'src/main/java/pl/pabilo8/immersiveintelligence'
    checks = 0

    def check(condition, message):
        nonlocal checks
        assert condition, message
        checks += 1

    jei = read('client/compat/CTMBProductionJEI.java')
    check(jei.index('CommonProxy.ITEMBLOCKS)', jei.index('public void register(')) < jei.index('for(Category category : categories)'), 'Blacklist must cover machines without categories')
    check('OreDictionary.WILDCARD_VALUE' in jei, 'Blacklist all metadata variants')
    native_jei = (ii / 'common/compat/jei/JEIHelper.java').read_text()
    check('getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE))' in native_jei, 'Blacklist contract differs from released II')

    component = read('common/gui/component/GuiComponent.java')
    screen = read('client/gui/MultiblockGui.java')
    for kind, clazz in [('bar_group', 'DecoBarGroup'), ('image', 'DecoImage'), ('gauge', 'DecoGauge')]:
        check(f'new GuiComponent("{kind}"' in component and f'case "{kind}":' in screen, f'{kind} lacks its screen binding')
        wrapper = read(f'client/gui/deco/CTMB{clazz}.java')
        check(f'extends {clazz} implements DecoComponentAccess' in wrapper, f'{clazz} must extend II')
        check('@ZenMethod @Override public IData getData()' in wrapper and '@ZenMethod @Override public void setData' in wrapper, f'{clazz} has no script state API')
    check('componentDefinitions().get(name)' in screen, 'Nested bar state must survive resizing')
    check('bar.withLimits(0, 10000, child.production()::progressValue)' in screen, 'Grouped production supplier is missing')
    check('bar.withLimits(0, child.source().getSize(), child.source()::getEnergy)' in screen, 'Grouped energy supplier is missing')
    check('instances.add(component)' in read('common/gui/MultiblockGuiLayout.java'), 'Shared component instances must be rejected')
    check('bars.forEach(GuiComponent::freeze)' in component, 'Nested bars must freeze with their plan')
    check('CTMBDecoGauge(x, y).withOnValueChanged' in screen, 'Gauge input is not bound to callbacks')
    check('withImageLocation(new net.minecraft.util.ResourceLocation(data.getString("image")), true)' in read('client/gui/deco/CTMBDecoData.java'), 'Image texture must use stitched atlas')

    state = read('common/block/BlockCTMBMultiblock.java')
    check(state.count('IEProperties.DYNAMICRENDER') >= 3 and 'return state.withProperty(IEProperties.DYNAMICRENDER, false)' in state, 'Static world blockstate must stay nondynamic')
    loader = read('common/util/ResourceLoader.java')
    check('variants.add("_1dynamicrender"' in loader and 'mb.animatedModel()==null?"immersiveengineering:ie_empty":mb.animatedModel()' in loader, 'Dynamic source must remain opt-in')
    check('f.getOpposite().getHorizontalAngle()' in loader and 'modelName+"_flipped.obj"' in loader, 'Supplied loader naming/rotation changed')
    renderer = read('client/render/CTMBAMTRenderer.java')
    check('endsWith(".obj.ie")' in renderer and 'getModelForState(state)' in renderer, 'Renderer must source an explicit blockstate AMT model')
    check('entry.model.defaultize()' in renderer and 'map.apply(progress)' in renderer, 'Render animation layers lack a reset')
    check('Entry::dispose' in renderer and 'iterator.remove()' in renderer, 'Model cache lacks disposal')
    check('withProperty(IEProperties.DYNAMICRENDER, true)' in renderer, 'Dynamic variant not selected')

    tactile = read('common/amt/CTMBTactileManager.java')
    native_manager = (ii / 'common/entity/tactile/TactileManager.java').read_text()
    check('managedEntities' in native_manager and 'managed().addAll(getEntities())' in tactile, 'Native entity ownership bridge differs')
    check('IIAnimationCollisionMap.create' in tactile and 'new EntityAMTTactile' in tactile, 'Native II maps/entities must be reused')
    check('EntityAMTTactile::setDead' in tactile and 'managed().clear()' in tactile, 'Cleanup must release native entities and ownership')
    check('CTMBAMTResources.read' in tactile, 'Tactile assets must support external files')
    tile = read('common/block/TileEntityMultiblock.java')
    check(tile.index('multiblock.onUpdate.execute') < tile.index('tactile.update(locations, times)'), 'Script samples must apply before tactile output')
    check('amtState.save()' in tile and 'amtState.restore' in tile, 'Samples must persist and sync')
    check('public void onChunkUnload()' in tile and 'public void invalidate()' in tile, 'Tactile unload hooks missing')
    check('world.isRemote||!formed' in tile and 'multiblock.tactileModel()==null' in tile, 'Tactile models must be opt-in and server-owned')
    check('INFINITE_EXTENT_AABB' in tile, 'Moving geometry must not be culled to static formation bounds')

    fixtures = ROOT / 'docs/examples/amt'
    header = json.loads((fixtures / 'crane.obj.amt').read_text())
    boxes = json.loads((fixtures / 'crane-tactile.json').read_text())
    animation = json.loads((fixtures / 'crane-rotate.json').read_text())
    allowed = set(re.findall(r'"([a-z_]+)"', read('common/amt/CTMBAMTHeader.java').split('private final Map', 1)[0]))
    for name, kind in header['types'].items():
        check(kind in allowed, f'Unknown fixture type {kind}')
        check(f'case "{kind}":' in read('client/render/CTMBAMTParts.java'), f'Fixture type {kind} has no factory')
    for child in header['hierarchy']:
        seen = set()
        part = child
        while part in header['hierarchy']:
            check(part not in seen, f'Cyclic fixture hierarchy at {part}')
            seen.add(part)
            part = header['hierarchy'][part]
    for name, parts in boxes['tactile'].items():
        if name.startswith('_'):
            continue
        check(name in header['origins'], f'Box part lacks a fixture origin: {name}')
        for part in parts:
            bounds = boxes['bounds'][part['type']]
            check(len(bounds) == 6 and all(math.isfinite(x) for x in bounds), 'Invalid fixture bounds')
            check(all(bounds[i] <= bounds[i+3] for i in range(3)), 'Inverted fixture bound')
    for name, group in animation['groups'].items():
        check(name in header['origins'], 'Animation part missing from header')
        line = group['rotation']
        check([frame['time'] for frame in line] == [0, 1], 'Invalid animation times')
        check(all(len(frame['transform']) == 3 for frame in line), 'Native animation vectors must have three axes')
    native_animation = (ii / 'common/util/amt/IIAnimation.java').read_text()
    check('json.has("groups")' in native_animation and 'obj.get("transform").getAsJsonArray()' in native_animation, 'Fixture animation format differs from II')

    # Independent numeric oracle for the selected export convention, all facings/mirrors.
    # Source contracts above check the selected angles/signs; this does not execute Matrix4.
    angles = {'north': 0, 'south': 180, 'east': 90, 'west': 270}
    for facing, angle in angles.items():
        for mirror in (False, True):
            x, y, z = 2, 3, 4
            centred = ((x - .5) * (-1 if mirror else 1), y - .5, z - .5)
            c, s = math.cos(math.radians(angle)), math.sin(math.radians(angle))
            transformed = (.5 + c*centred[0]+s*centred[2], .5+centred[1], .5-s*centred[0]+c*centred[2])
            inverse = (c*(transformed[0]-.5)-s*(transformed[2]-.5), transformed[1]-.5, s*(transformed[0]-.5)+c*(transformed[2]-.5))
            inverse = (.5+inverse[0]*(-1 if mirror else 1), .5+inverse[1], .5+inverse[2])
            check(all(abs(a-b) < 1e-9 for a, b in zip(inverse, (x, y, z))), f'Transform oracle roundtrip failed: {facing}/{mirror}')
    check('Math.toRadians(facing.getOpposite().getHorizontalAngle())' in read('common/amt/CTMBModelTransform.java') and 'rotate(tile.facing.getOpposite().getHorizontalAngle(), 0, 1, 0)' in renderer, 'Server/render facing matrix contract differs')
    check('if(mirrored) matrix.scale(-1, 1, 1)' in read('common/amt/CTMBModelTransform.java') and 'GlStateManager.scale(-1, 1, 1)' in renderer, 'Server/render mirror contract differs')
    print(f'PASS: {checks} static API/resource/geometry-oracle contracts against released II source.')
    print('Java/ZenScript compilation, JUnit execution and Minecraft runtime checks are not performed.')


if __name__ == '__main__':
    main()
