// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

/** Axis normalization shared by host tests and Android navigation. */
final class ControllerPolicy {
 static int direction(float x,float y,float flat){
  float dead=Math.max(.25f,Math.min(.8f,flat));
  if(!Float.isFinite(x)||!Float.isFinite(y)||Math.max(Math.abs(x),Math.abs(y))<=dead)return 0;
  if(Math.abs(x)>Math.abs(y))return x<0?21:22; // Android DPAD_LEFT/RIGHT
  return y<0?19:20; // Android DPAD_UP/DOWN
 }
 private ControllerPolicy(){}
}
