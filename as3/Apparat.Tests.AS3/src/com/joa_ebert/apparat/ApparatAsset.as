package com.joa_ebert.apparat 
{
	import flash.display.Sprite;
	import flash.display.StageQuality;
	import flash.display.StageScaleMode;

	[SWF(width='640', height='480', frameRate='32', backgroundColor='0x000000')]
	/**
	 * @author Joa Ebert
	 */
	public class ApparatAsset extends Sprite 
	{
		public function ApparatAsset()
		{
			stage.frameRate = 999;
			stage.scaleMode = StageScaleMode.NO_SCALE;
			stage.quality = StageQuality.LOW;
		}
	}
}
