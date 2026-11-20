// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;
import java.util.Arrays;
import java.util.Random;

public final class FoldControllerTest {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static void pane(int[] actual,int... expected){check(Arrays.equals(actual,expected),"Pane: "+Arrays.toString(actual));}
 public static void main(String[] args){
  pane(FoldLayoutPolicy.safePane(1800,1200,890,0,910,1200,true,true),0,0,890,1200);
  pane(FoldLayoutPolicy.safePane(1800,1200,600,0,620,1200,true,true),620,0,1800,1200);
  pane(FoldLayoutPolicy.safePane(1800,1200,900,0,900,1200,true,true),0,0,900,1200);
  pane(FoldLayoutPolicy.safePane(1080,2200,0,1080,1080,1120,false,true),0,0,1080,1080);
  pane(FoldLayoutPolicy.safePane(1800,1200,900,0,900,1200,true,false),0,0,1800,1200);
  pane(FoldLayoutPolicy.safePane(500,1200,900,0,920,1200,true,true),0,0,500,1200);
  pane(FoldLayoutPolicy.safePane(500,800,-20,0,0,800,true,true),0,0,500,800);
  pane(FoldLayoutPolicy.safePane(500,800,240,900,260,1000,true,true),0,0,500,800);
  // Random valid fold rectangles must never occlude the chosen content region.
  Random random=new Random(107);
  for(int i=0;i<2000;i++){
   int w=300+random.nextInt(2200),h=300+random.nextInt(2200);
   boolean vertical=random.nextBoolean();int size=vertical?w:h;
   int start=1+random.nextInt(size-2),end=start+random.nextInt(size-start);
   int[] p=FoldLayoutPolicy.safePane(w,h,vertical?start:0,vertical?0:start,vertical?end:w,vertical?h:end,vertical,true);
   check(p[0]>=0&&p[1]>=0&&p[2]<=w&&p[3]<=h&&p[2]>p[0]&&p[3]>p[1],"Bounds");
   check(vertical?(p[2]<=start||p[0]>=end):(p[3]<=start||p[1]>=end),"No overlap");
  }
  check(ControllerPolicy.direction(.2f,.1f,0)==0,"Stick drift");
  check(ControllerPolicy.direction(.25f,0,0)==0,"Threshold is neutral");
  check(ControllerPolicy.direction(.5f,0,.6f)==0,"Device deadzone");
  check(ControllerPolicy.direction(-1,0,0)==21,"Left");
  check(ControllerPolicy.direction(1,0,0)==22,"Right");
  check(ControllerPolicy.direction(0,-1,0)==19,"Up");
  check(ControllerPolicy.direction(0,1,0)==20,"Down");
  check(ControllerPolicy.direction(.8f,-.9f,0)==19,"Dominant axis");
  check(ControllerPolicy.direction(Float.NaN,1,0)==0,"Invalid axis");
  check(ControllerPolicy.direction(1,Float.POSITIVE_INFINITY,0)==0,"Infinite axis");
  System.out.println("Fold/controller policy: "+checks+" checks passed");
 }
}
