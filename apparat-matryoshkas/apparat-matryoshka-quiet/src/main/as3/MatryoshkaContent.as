package {
	import flash.utils.ByteArray;

	/**
	 * @author Joa Ebert
	 */
	public final class MatryoshkaContent {
		[Embed(source='../resources/EMPTY_FILE', mimeType='application/octet-stream')]
		public static var byteArrayAsset: Class
		public static var byteArray: ByteArray = ByteArray(new byteArrayAsset())
	}
}