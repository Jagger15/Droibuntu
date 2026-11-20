#!/usr/bin/env python3
"""Reconstruct the complete public Test107 source from the verified Test106 release."""
import argparse,hashlib,json,pathlib,shutil,urllib.request,zipfile,tempfile
HERE=pathlib.Path(__file__).resolve().parent
BASE_URL='https://github.com/Jagger15/Droibuntu/releases/download/test106-separate/Droibuntu-Test106-Separate-source.zip'
BASE_SHA='9fa2c52f06b0e4293b9c823097018857eff28c36d5d8902d977c5f4290aabc63'
def download(url,path,digest):
 with urllib.request.urlopen(url,timeout=120) as response,open(path,'wb') as output:shutil.copyfileobj(response,output)
 if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:raise RuntimeError('SHA-256 mismatch: '+path.name)
def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('destination',type=pathlib.Path);parser.add_argument('--baseline',type=pathlib.Path,help='Use an already downloaded Test106 source ZIP')
 args=parser.parse_args();destination=args.destination.resolve()
 if destination.exists():parser.error('Destination must not exist; existing work is never overwritten')
 destination.parent.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory(prefix='droibuntu107-',dir=destination.parent) as temporary:
  stage=pathlib.Path(temporary);archive=args.baseline
  if archive is None:archive=stage/'baseline.zip';download(BASE_URL,archive,BASE_SHA)
  elif hashlib.sha256(archive.read_bytes()).hexdigest()!=BASE_SHA:raise RuntimeError('Wrong Test106 baseline')
  with zipfile.ZipFile(archive) as source:
   for item in source.infolist():
    p=pathlib.PurePosixPath(item.filename)
    if p.is_absolute() or '..' in p.parts or not p.parts or p.parts[0]!='Droibuntu' or (item.external_attr>>16)&0o170000==0o120000:raise RuntimeError('Unsafe archive path')
   source.extractall(stage)
  project=stage/'Droibuntu';shutil.copytree(HERE/'overlay',project,dirs_exist_ok=True)
  deps=project/'third_party/foldable';deps.mkdir(parents=True,exist_ok=True)
  for record in json.loads((deps/'dependencies.json').read_text()):download(record['url'],deps/record['file'],record['sha256'])
  shutil.move(str(project),str(destination))
 print(destination)
if __name__=='__main__':main()
