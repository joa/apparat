
	Reducer

	
	NOTE: You will need at least Java 1.6 SE to run this tool.

	A tool to reduce the file size of SWFs by making use of the full
	feature set the Flash Player provides and which is ignored by
	the ActionScript compiler.
	
		java -jar reducer.jar -input old.swf -output new.swf

		java -jar reducer.jar -input old.swf -output new.swf -quality 0.5


	The default settings are:
	
		quality is 0.5
		deblock is 0.0
		keep-premultiplied is true.

	The FlashPlayer seems to have problems with pictures that contain an alpha
	channel and have the deblocking filter applied.

	The original.swf has been compiled without using Reducer. The other files
	have been optimized using reducer and the corresponding quality value.


	- It is important to note that reducer will convert lossless images to
	lossy images. 

	- Reducer will only touch lossless images and not touch you encoded JPEG
	images.

	- The best way to reducer is embedding PNG files with an alpha channel and
	then using the -quality 1.0 setting. This will keep the best quality but
	your SWF will still shrink a lot.

	The quality_1.00.swf and original.swf are an example for this case.



	Best,

	Joa