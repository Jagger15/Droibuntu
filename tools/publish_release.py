#!/usr/bin/env python3
"""Create a draft GitHub release from locally reviewed APK and public source files."""
import argparse,hashlib,pathlib,re,subprocess,tempfile,zipfile

def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--repository',default='Jagger15/Droibuntu')
 p.add_argument('--tag',required=True)
 p.add_argument('--apk',type=pathlib.Path,required=True)
 p.add_argument('--source',type=pathlib.Path,required=True)
 p.add_argument('--notes',type=pathlib.Path,required=True)
 p.add_argument('--publish',action='store_true',help='Publish immediately; default is a draft')
 a=p.parse_args()
 if not re.fullmatch(r'[A-Za-z0-9-]+/[A-Za-z0-9._-]+',a.repository):p.error('Invalid repository')
 if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]*',a.tag):p.error('Invalid tag')
 for f in (a.apk,a.source,a.notes):
  if not f.is_file():p.error('Missing file: '+str(f))
 if a.apk.suffix.lower()!='.apk' or a.source.suffix.lower()!='.zip':p.error('Provide one APK and a source ZIP')
 with zipfile.ZipFile(a.source) as z:
  if any(pathlib.Path(n).suffix.lower() in {'.jks','.keystore','.pem','.p12','.pfx'} or pathlib.Path(n).name.startswith('.env') for n in z.namelist()):p.error('Source ZIP contains a signing/secret file; use the public source archive')
 sums=''.join(hashlib.sha256(f.read_bytes()).hexdigest()+'  '+f.name+'\n' for f in (a.apk,a.source))
 with tempfile.TemporaryDirectory() as tmp:
  checks=pathlib.Path(tmp)/'SHA256SUMS';checks.write_text(sums)
  cmd=['gh','release','create',a.tag,'--repo',a.repository,'--title','Droibuntu '+a.tag,'--notes-file',str(a.notes)]
  cmd+=['--latest'] if a.publish else ['--draft']
  cmd += [str(a.apk.resolve()),str(a.source.resolve()),str(checks)]
  subprocess.run(cmd,check=True)
 print('Published release' if a.publish else 'Draft created; review assets before publishing')
if __name__=='__main__':main()
