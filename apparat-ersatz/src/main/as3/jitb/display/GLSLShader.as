package jitb.display {
	import flash.display.Shader
	import flash.errors.IllegalOperationError

	public class GLSLShader extends Shader {
		public function GLSLShader(fragmentShader: String,
															 vertexShader: String = "void main(){gl_Position=ftransform();}") {
			throw new IllegalOperationError();
		}

		public function dispose(): void  { throw new IllegalOperationError(); }
	}
}