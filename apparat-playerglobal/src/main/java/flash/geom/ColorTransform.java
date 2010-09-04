package flash.geom;

/**
 * @author Joa Ebert
 */
public class ColorTransform extends jitb.lang.Object {
	public double alphaOffset;
	public double alphaMultiplier;
	public double redOffset;
	public double redMultiplier;
	public double greenOffset;
	public double greenMultiplier;
	public double blueOffset;
	public double blueMultiplier;

	public ColorTransform() {
		this(1.0);
	}

	public ColorTransform(final double redMultiplier) {
		this(redMultiplier, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier) {
		this(redMultiplier, greenMultiplier, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier,
						  final double blueMultiplier) {
		this(redMultiplier, greenMultiplier, blueMultiplier, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier,
						  final double blueMultiplier, final double alphaMultiplier) {
		this(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier,
						  final double blueMultiplier, final double alphaMultiplier,
						  final double redOffset) {
		this(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier, redOffset, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier,
						  final double blueMultiplier, final double alphaMultiplier,
						  final double redOffset, final double greenOffset) {
		this(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier,
			 redOffset, greenOffset, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier,
						  final double blueMultiplier, final double alphaMultiplier,
						  final double redOffset, final double greenOffset,
						  final double blueOffset) {
		this(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier,
			 redOffset, greenOffset, blueOffset, 1.0);
	}

	public ColorTransform(final double redMultiplier, final double greenMultiplier, 
						  final double blueMultiplier, final double alphaMultiplier,
						  final double redOffset, final double greenOffset,
						  final double blueOffset, final double alphaOffset) {
		this.redMultiplier = redMultiplier;
		this.greenMultiplier = greenMultiplier;
		this.blueMultiplier = blueMultiplier;
		this.alphaMultiplier = alphaMultiplier;
		this.redOffset = redOffset;
		this.greenOffset = greenOffset;
		this.blueOffset = blueOffset;
		this.alphaOffset = alphaOffset;
	}
	public long color() {
		return (((long)redOffset) << 0x10) | (((long)greenOffset) << 0x08) | ((long)blueOffset);
	}

	public void color(final long value) {
		redMultiplier = greenMultiplier = blueMultiplier = 0.0;
		redOffset = (double)((value & 0xff0000L) >> 0x10);
		greenOffset = (double)((value & 0xff00L) >> 0x08);
		blueOffset = (double)(value & 0xffL);
	}

	public void concat(final ColorTransform second) {
		alphaOffset += second.alphaOffset;
		redOffset += second.redOffset;
		greenOffset += second.greenOffset;
		blueOffset += second.blueOffset;

		alphaMultiplier *= second.alphaMultiplier;
		redMultiplier *= second.redMultiplier;
		greenMultiplier *= second.greenMultiplier;
		blueMultiplier *= second.blueMultiplier;
	}

	@Override
	public String toString() {
		return "(redMultiplier="+redMultiplier+", greenMultiplier="+greenMultiplier+", blueMultiplier="+blueMultiplier+", "+
			"alphaMultiplier="+alphaMultiplier+", redOffset="+redOffset+", greenOffset="+greenOffset+", blueOffset="+blueOffset+", alphaOffset="+alphaOffset+")";
	}
}
