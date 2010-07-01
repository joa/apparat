
	Dump

	
	NOTE: You will need at least Java 1.6 SE to run this tool.

	The Dump tool offers detailed insight into Apparat's internal representation
	of ABC files. Besides all known tags of an SWC/SWF file may be shown as
	well.
	
	This tool is not a decompiler and you should only use it for debugging 
	purposes. 

	Pretty-print ABC files:
	
		java -jar dump.jar -input test.swf -abc
		
	Export DefineBitsJPEG2 images:
	
		java -jar dump.jar -input test.swf -images
		
	Show known tags:	
	
		java -jar dump.jar -input test.swf -tags
		

	Best,

	Joa