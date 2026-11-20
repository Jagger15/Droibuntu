#!/usr/bin/env python3
"""Pinned local Android SDK build for phone and Wear OS. No repository/publishing required."""
import os,pathlib,subprocess,zipfile,json,shutil,sys,hashlib
ROOT=pathlib.Path(__file__).resolve().parents[1];os.chdir(ROOT)
SDK=pathlib.Path(os.environ['BUILD_TOOLS']);ANDROID=pathlib.Path(os.environ['ANDROID_JAR']);ECJ=pathlib.Path(os.environ['ECJ_JAR'])
def run(args):subprocess.run([str(a) for a in args],check=True)
deps=ROOT/'third_party/wear';records=json.loads((deps/'dependencies.json').read_text());jars=[];resources=[];packages=[]
base=pathlib.Path(os.environ.get('DROIBUNTU_BUILD_WORK',str(ROOT/'build/wear-release')));base.mkdir(parents=True,exist_ok=True)
if len(sys.argv)<2 or sys.argv[1] in ('both','phone','check'):
 fold_deps=ROOT/'third_party/foldable'
 fold_records=json.loads((fold_deps/'dependencies.json').read_text())
 replaced={('androidx.core','core'),('androidx.annotation','annotation'),('androidx.lifecycle','lifecycle-runtime'),('androidx.lifecycle','lifecycle-common'),('androidx.versionedparcelable','versionedparcelable')}
 records=[dict(record, directory=str(deps)) for record in records if (record['group'],record['artifact']) not in replaced]+[dict(record,directory=str(fold_deps)) for record in fold_records]
for record in records:
 source_dir=pathlib.Path(record.get('directory',str(deps)))
 path=source_dir/record['file']
 if hashlib.sha256(path.read_bytes()).hexdigest()!=record['sha256']:raise RuntimeError('Dependency hash mismatch '+path.name)
 if path.suffix=='.jar':jars.append(path);continue
 directory=base/(record['group']+'-'+record['artifact']+'-'+record['version']);shutil.rmtree(directory,ignore_errors=True);directory.mkdir();
 with zipfile.ZipFile(path) as source:source.extractall(directory)
 jars.append(directory/'classes.jar');jars.extend((directory/'libs').glob('*.jar'))
 manifest=directory/'AndroidManifest.xml'
 import xml.etree.ElementTree as ET
 package=ET.parse(manifest).getroot().get('package')
 if package:packages.append(package)
 if (directory/'res').exists() and any((directory/'res').rglob('*.*')):
  compiled=base/(directory.name+'-resources.zip');run([SDK/'aapt2','compile','--dir',directory/'res','-o',compiled]);resources.append(compiled)

def build(watch=False):
 build_jars=list(jars);media=None;media_resources=[];extra_packages=list(packages)
 if not watch:
  record=json.loads((ROOT/'third_party/media/dependency.json').read_text());aar=ROOT/'third_party/media'/record['file']
  if hashlib.sha256(aar.read_bytes()).hexdigest()!=record['sha256']:raise RuntimeError('libVLC dependency hash mismatch')
  media=base/'libvlc-sdk';shutil.rmtree(media,ignore_errors=True);media.mkdir()
  with zipfile.ZipFile(aar) as z:z.extractall(media)
  build_jars.append(media/'classes.jar');extra_packages.append(ET.parse(media/'AndroidManifest.xml').getroot().get('package'))
  compiled=base/'libvlc-resources.zip';run([SDK/'aapt2','compile','--dir',media/'res','-o',compiled]);media_resources.append(compiled)
 work=base/('watch' if watch else 'phone');shutil.rmtree(work,ignore_errors=True);work.mkdir()
 project=ROOT/'wear' if watch else ROOT
 manifest=project/'AndroidManifest.xml'
 res=work/'resources.zip';run([SDK/'aapt2','compile','--dir',project/'res','-o',res])
 generated=work/'generated';generated.mkdir()
 linked=work/'base.apk'
 command=[SDK/'aapt2','link','-o',linked,'-I',ANDROID,'--manifest',manifest,'--java',generated,'--extra-packages',':'.join(sorted(set(extra_packages))),'--auto-add-overlay','--min-sdk-version','26','--target-sdk-version','34' if watch else '28']
 if not watch:command.extend(['--custom-package','org.ubuntuquest.pocket'])
 for resource in resources+media_resources:command.extend(['-R',resource])
 command.extend(['-R',res])
 if not watch:
  assets=work/'assets';shutil.copytree(ROOT/'assets',assets,ignore=shutil.ignore_patterns('__pycache__','*.pyc','.*'));
  if media is not None and (media/'assets').exists():shutil.copytree(media/'assets',assets,dirs_exist_ok=True)
  command.extend(['-A',assets])
 run(command)
 classes=work/'classes';classes.mkdir()
 sources=list((project/'src').rglob('*.java'))+list(generated.rglob('*.java'))
 run(['java','-jar',ECJ,'-8','-proc:none','-nowarn','-cp',os.pathsep.join(map(str,[ANDROID]+build_jars)),'-d',classes]+sources)
 archive=work/'classes.jar'
 with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
  for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
 dex=work/'dex';dex.mkdir()
 run(['java','-cp',SDK/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',ANDROID,'--output',dex,archive]+build_jars)
 if 'check' in sys.argv:
  print('Phone Java/resources/D8 checks passed; no installable APK produced.',flush=True);return
 unsigned=work/'unsigned.apk';shutil.copyfile(linked,unsigned)
 with zipfile.ZipFile(unsigned,'a',zipfile.ZIP_DEFLATED) as z:
  for p in dex.glob('*.dex'):z.write(p,p.name)
  if not watch:
   for p in (ROOT/'lib').rglob('*.so'):z.write(p,p.relative_to(ROOT))
   if media is not None:
    for p in (media/'jni/arm64-v8a').glob('*.so'):z.write(p,'lib/arm64-v8a/'+p.name)
 aligned=work/'aligned.apk';run([SDK/'zipalign','-f','-p','4',unsigned,aligned])
 if 'unsigned' in sys.argv:
  version=ET.parse(manifest).getroot().get('{http://schemas.android.com/apk/res/android}versionCode');out=ROOT/'dist'/('Droibuntu-Test'+version+'-unsigned.apk');out.parent.mkdir(exist_ok=True);shutil.copyfile(aligned,out);run([SDK/'zipalign','-c','-p','4',out]);run([SDK/'aapt2','dump','badging',out]);print(out.name,out.stat().st_size,hashlib.sha256(out.read_bytes()).hexdigest(),flush=True);return
 version=ET.parse(manifest).getroot().get('{http://schemas.android.com/apk/res/android}versionCode');out=ROOT/'dist'/('Droibuntu-Watch-Test'+version+'.apk' if watch else 'Droibuntu-Test'+version+'.apk');out.parent.mkdir(exist_ok=True)
 run(['java','-jar',SDK/'lib/apksigner.jar','sign','--ks',pathlib.Path(os.environ.get('DROIBUNTU_KEYSTORE',str(ROOT/'tools/test-signing.jks'))),'--ks-pass','pass:android','--key-pass','pass:android','--out',out,aligned])
 run(['java','-jar',SDK/'lib/apksigner.jar','verify','--verbose','--print-certs',out]);run([SDK/'zipalign','-c','-p','4',out]);run([SDK/'aapt2','dump','badging',out]);print(out.name,out.stat().st_size,hashlib.sha256(out.read_bytes()).hexdigest(),flush=True)
run(['sha256sum','-c','third_party/SHA256SUMS'])
mode=sys.argv[1] if len(sys.argv)>1 else 'both'
if mode in ('both','phone','check'):build(False)
if mode in ('both','watch'):build(True)
