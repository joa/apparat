    
	Apparat
	http://apparat.googlecode.com/
	
	Apparat is a framework to work with ABC, SWC and SWF files. You can use the core framework
    to build scripted applications that modify the content of a SWF file or use any of the
	predefined available tools.
	
	All tools will show their usage information if you omit any parameters. Apparat tries to make
	use of an advanced compression with 7-Zip. If Apparat can find 7-Zip on your PATH it will use
	it to compress your content.
	To test if 7-Zip is available you should simply enter "7z" on Windows or "7za" on Linux/OS X
	in the command line.
	
	- Apparat Shell
	
	  The shell is a tool to spawn Apparat only once and keep it running. Since it is a Java 
	  application you will save JVM startup time and the overhead to allocate threadpools when
	  running Apparat. 
	  
	  The shell has been created to work asynchronous with multiple requests so any number
	  of applications and requests can run simoultaneously.
	  
	  All predefined tools can be executed from the shell just like from the command line.
	  
	- Concrete
	  
	  The Concrete tool allows you to speficy abstract methods and check at compile time
	  if they are overriden. To mark a method abstract you add the [Abstract] metadata to it.
	  
	  When compiling your project you need to keep this metadata. This is done by specifying
	  "-keep-as3-metadata=Abstract" as a compiler argument.

      Concrete takes one parameter "-i" which is the list of input files. You will need to include
	  all SWC or SWF files that have been used to compile this project. This means even for
	  a simple project you have to specify playerglobal.swc for instance since it is used to
	  compile your project.
	  
	  To seperate multiple libraries use your systems path separator character. This is ";" on 
	  Windows machines and ":" on Mac OS X or Linux.
	  
	  Example (Windows):
	    concrete -i test.swf;C:\path\to\playerglobal.swc
		
	  Example (Linux/OS X):
		concrete -i test.swf:/path/to/playerglobal.swc
	
	- Coverage
	
	  With the Coverage tool you can insert coverage information into your code. Apparat assumes
	  that a class "apparat.coverage.Coverage" exists or is provided at runtime.
	  
	  An example of the Coverage class can look like this:
	  
	  package apparat.coverage {
	    public final class Coverage {
		  public static function onSample(file: String, line: int): void {
		    trace("Touched line", line, "in", file);
		  }
		}
	  }
	  
	  To run coverage you specify an input file with the "-i" parameter and an optional output file
	  with the "-o" parameter.
	  
	  You can add multiple source-paths in the "-s" parameter. The tool will instrument only files
	  that are also on the source-path. Like the Concrete tool you can chain multiple paths with 
	  the path separator of your operating system.
	  
	  Example:
	    coverage -i input.swf -o output.swf -s C:\path\to\as3\source
	
	- Dump
	
	  This tool can be used to generate detailed information of a given file.
	  
	  Specify the input with the "-i" parameter. An optional directory can be given with the "-o"
	  parameter. Dump will output all files in this directory which is by default the directory
	  of the given input file.
	  
	  If you speficy the "-swf" parameter the tag information of the file is exported. If you
	  speficy the "-uml" parameter a UML graph for the given file is generated in DOT format. This
	  format can be opened with OmniGraffle in OS X or you can transform it to an image or SVG
	  with Graphviz.
	  
	  If you spefiy the "-abc" parameter, dump will output detailed ABC information. If "-abc" is 
	  specified you can also change the way how methods are written. "-bc raw" will show raw bytes,
	  "-bc cfg" will output methods a s control flow graphs in DOT format. "-bc default" will use
	  Apparat's default bytecode representation.
	  
	- Reducer
	
	  You can use reducer for advanced compression of your SWF files. Reducer tries to compress
	  embedded PNG graphics. You can leverage this option also with the ActionScript compiler
	  by specifing "[Embed(src=..., compress=true)]". However to speedup compilation you can ignore
	  the compress parameter and let Reducer do the job since it makes use of multicore 
	  architectures.
	  
	  "-i" specifies the input file, "-o" an optional output file. "-q" specifies the JPEG 
	  compression level. "-q 1.0" is maximum quality, "-q 0.0" is minimum quality. You will get
      also good compression results for "-q 1.0". "-d" speficies the strength of the Flash Players
      internal deblocking filter.

      Example:
        reducer -i input.swf -o output.swf -q 0.96	  
	
	- Stripper
	
	  This tool removes all debug information from a SWF file. It is a type-safe removal keeping
	  side-effects. This means a loop like this
	  
	  while(iter.hasNext) { trace(iter.next()) }
	  
	  Would be rewritten like
	  
	  while(iter.hasNext) { iter.next() }
	  
	  Stripper removes also all debug releated bytecode.
	  
	  Example:
	    stripper -i input.swf -o output.swf
		stripper -i inputAndOutput.swc
		
	- Turbo Diesel Sport Injection
	
	  The TDSI tool performs various bytecode transformations. Besides specific transformations the
	  application will always try to do certain peephole optimizations. Most of them will fix
	  problems with older ActionScript compiler versions.
	  
	  -f [true|false]
	  If you specify the "-f" argument TDSI will try to fix certain problems with files generated
	  by the Alchemy compiler. This transformation will only affect code generated from C/C++ 
	  sources. This option defaults to false. The best way to optimize an Alchemy file with TDSI
	  is by calling "tdsi -i input.swc -o output.swc -f true -a false -e false -m false".
	  
	  This transformation is by default turned off.
	  
	  -a [true|false]
	  This option will inline Alchemy operations from ActionScript. If you use the Memory class 
	  provided by the apparat-ersatz library those operations will be replaced with fast Alchemy
	  op codes. More information is available at http://code.google.com/p/apparat/wiki/MemoryPool
	  
	  This transformation is by default turned on.

      -e [true|false]
	  Perform inline expansion. If your class extends the apparat.inline.Inline class all its static
	  methods will be inlined when called. Those methods may not contain exceptions and must
	  be static.
	  
	  This transformation is by default turned on.
	  
	  -m [true|false]
	  Whether or not to enable macro expansion. Macros are like a type-safe copy and paste that 
	  happens at compile time. More information is available here:
	  http://code.google.com/p/apparat/wiki/MemoryPool
	  
	  Example:
	    Perform alchemy-, inline- and macroexpansion
	    tdsi -i input.swf -o output.swf
		
		Optimize a SWC generated by Alchemy
		tdsi -i input.swc -o output.swc -f true -a false -e false -m false
		
		Optimize a SWC generated by Alchemy will all other features turned on
		tdsi -i input.swc -o output.swc -f