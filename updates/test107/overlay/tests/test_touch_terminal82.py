import pathlib,sys,unittest,os,pty,select,signal,struct,fcntl,termios,time
sys.path.insert(0,str(pathlib.Path(__file__).resolve().parents[1]/'assets'))
from terminal_policy import KEYS,stroke,paste_review,bounded_font
class TerminalPolicyTest(unittest.TestCase):
 def test_modifiers_and_unicode(self):
  self.assertEqual(stroke('c',True),'\x03');self.assertEqual(stroke('a',False,True),'\x1ba');self.assertEqual(stroke('こんにちは'),'こんにちは')
  with self.assertRaises(ValueError):stroke('multiple',True)
  with self.assertRaises(ValueError):stroke('a\0')
 def test_paste_review_and_limits(self):
  for text in ['echo x\n','a\r','\x1b[A','x'*257]:self.assertTrue(paste_review(text))
  self.assertFalse(paste_review('plain text'))
  with self.assertRaises(ValueError):paste_review('x'*65537)
  with self.assertRaises(ValueError):paste_review('\0')
 def test_font_bounds(self):self.assertEqual(bounded_font(1000),36);self.assertEqual(bounded_font(1),10);self.assertEqual(bounded_font('1000'),16)
class LivePtyTest(unittest.TestCase):
 def setUp(self):
  self.pid,self.fd=pty.fork()
  if self.pid==0:os.execve('/bin/bash',['bash','--noprofile','--norc','-i'],dict(os.environ,PS1='DROIBUNTU-PTY> '))
  self.addCleanup(self.cleanup_pty)
  self.read_until(b'DROIBUNTU-PTY> ',3)
 def cleanup_pty(self):
  try:os.close(self.fd)
  except OSError:pass
  try:os.kill(self.pid,signal.SIGKILL)
  except ProcessLookupError:pass
  try:os.waitpid(self.pid,0)
  except ChildProcessError:pass
 def read_until(self,needle,timeout=3):
  end=time.monotonic()+timeout;data=b''
  while time.monotonic()<end:
   if select.select([self.fd],[],[],.1)[0]:
    try:data+=os.read(self.fd,65536)
    except OSError:break
    if needle in data:return data
  self.fail('PTY did not produce '+repr(needle)+': '+repr(data[-400:]))
 def test_resize_keeps_shell_and_changes_terminal_size(self):
  fcntl.ioctl(self.fd,termios.TIOCSWINSZ,struct.pack('HHHH',36,120,0,0));os.write(self.fd,b'stty size\r');self.read_until(b'36 120');self.assertEqual(os.waitpid(self.pid,os.WNOHANG),(0,0))
 def test_ctrl_c_interrupts_foreground_job(self):
  os.write(self.fd,b'sleep 20\r');time.sleep(.1);os.write(self.fd,KEYS['Ctrl+C'].encode());os.write(self.fd,b'printf "PTY-OK\\n"\r');self.read_until(b'PTY-OK\r\n')
if __name__=='__main__':unittest.main()
