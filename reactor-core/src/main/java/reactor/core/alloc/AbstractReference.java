package reactor.core.alloc;

import reactor.support.TimeUtils;

/**
 * An abstract {@link reactor.core.alloc.Reference} implementation that does reference counting.
 *
 * @author Jon Brisbin
 * @since 1.1
 */
public abstract class AbstractReference<T extends Recyclable> implements Reference<T> {

	private volatile int refCnt = 0;

	private final long inception;
	private final T    obj;

	protected AbstractReference(T obj) {
		this.obj = obj;
		this.inception = TimeUtils.approxCurrentTimeMillis();
	}

	@Override
	public long getAge() {
		return TimeUtils.approxCurrentTimeMillis() - inception;
	}

	@Override
	public int getReferenceCount() {
		return refCnt;
	}

	@Override
	public void retain() {
		retain(1);
	}

	@Override
	public void retain(int incr) {
		refCnt += incr;
	}

	@Override
	public void release() {
		release(1);
	}

	@Override
	public void release(int decr) {
		refCnt -= Math.min(decr, refCnt);
		if(refCnt < 1) {
			obj.recycle();
		}
	}

	@Override
	public T get() {
		return obj;
	}

	@Override
	public String toString() {
		return "Reference{" +
				"refCnt=" + refCnt +
				", inception=" + inception +
				", obj=" + obj +
				'}';
	}

}
