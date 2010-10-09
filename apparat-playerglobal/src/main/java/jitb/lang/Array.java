package jitb.lang;

import jitb.errors.MissingImplementationException;
import jitb.lang.closure.Function;
import jitb.lang.closure.Function3;

import java.lang.*;
import java.util.*;

/**
 * @author Joa Ebert
 */
public class Array extends jitb.lang.Object {
	public static final long CASEINSENSITIVE = 1;
	public static final long DESCENDING = 2;
	public static final long UNIQUESORT = 4;
	public static final long RETURNINDEXEDARRAY = 8;
	public static final long NUMERIC = 16;

	private final ArrayList<java.lang.Object> _arrayList;

	public Array() {
		this(0);
	}
	
	public Array(final int numElements) {
		_arrayList = new ArrayList<java.lang.Object>(numElements);
	}

	public Array(final java.lang.Object ...arguments) {
		final List<java.lang.Object> argumentList = Arrays.asList(arguments);
		
		_arrayList = argumentList instanceof ArrayList ?
				(ArrayList<java.lang.Object>)argumentList : new ArrayList<java.lang.Object>();
	}

	public Array(final ArrayList<java.lang.Object> arrayList) {
		_arrayList = arrayList;
	}

	public long length() {
		return _arrayList.size();
	}

	public Array concat(final java.lang.Object ...arguments) {
		final int n = arguments.length;
		final Array result = new Array(arguments.length);
		final ArrayList<java.lang.Object> list = result._arrayList;
		
		list.addAll(_arrayList);

		for(int i = 0; i  < n; ++i) {
			final java.lang.Object value = arguments[i];

			if(value instanceof Array) {
				list.addAll(((Array)value)._arrayList);
			} else {
				list.add(value);
			}
		}

		return result;
	}

	public boolean every(final Function3<java.lang.Object, Integer, Array, Boolean> callback) {
		return every(callback, null);
	}

	public boolean every(final Function3<java.lang.Object, Integer, Array, Boolean> callback, final jitb.lang.Object thisObject) {
		final int n = _arrayList.size();

		for(int i = 0; i < n; ++i) {
			if(!callback.apply3(thisObject, _arrayList.get(i), i, this)) {
				return false;
			}
		}

		return true;
	}

	public boolean every(final Function<Boolean> callback) {
		return every((Function3<java.lang.Object, Integer, Array, Boolean>)callback, null);
	}

	public boolean every(final Function<Boolean> callback, final jitb.lang.Object thisObject) {
		return every((Function3<java.lang.Object, Integer, Array, Boolean>)callback, thisObject);
	}

	public Array filter(final Function3<java.lang.Object, Integer, Array, Boolean> callback) {
		return filter(callback, null);
	}

	public Array filter(final Function3<java.lang.Object, Integer, Array, Boolean> callback, final jitb.lang.Object thisObject) {
		final Array result = new Array();
		final int n = _arrayList.size();

		for(int i = 0; i < n; ++i) {
			final java.lang.Object value = _arrayList.get(i);

			if(callback.apply3(thisObject, value, i, this)) {
				result._arrayList.add(value);
			}
		}

		return result;
	}

	public Array filter(final Function<Boolean> callback) {
		return filter((Function3<java.lang.Object, Integer, Array, Boolean>)callback, null);
	}

	public Array filter(final Function<Boolean> callback, final jitb.lang.Object thisObject) {
		return filter((Function3<java.lang.Object, Integer, Array, Boolean>)callback, thisObject);
	}

	public boolean forEach(final Function3<java.lang.Object, Integer, Array, java.lang.Object> callback) {
		return forEach(callback, null);
	}

	public boolean forEach(final Function3<java.lang.Object, Integer, Array, java.lang.Object> callback, final jitb.lang.Object thisObject) {
		final int n = _arrayList.size();

		for(int i = 0; i < n; ++i) {
			callback.applyVoid3(thisObject, _arrayList.get(i), i, this);
		}

		return true;
	}

	public boolean forEach(final Function<java.lang.Object> callback) {
		return forEach((Function3<java.lang.Object, Integer, Array, java.lang.Object>)callback, null);
	}

	public boolean forEach(final Function<java.lang.Object> callback, final jitb.lang.Object thisObject) {
		return forEach((Function3<java.lang.Object, Integer, Array, java.lang.Object>)callback, thisObject);
	}

	public int indexOf(final java.lang.Object searchValue) {
		return _arrayList.indexOf(searchValue);
	}

	public int indexOf(final java.lang.Object searchValue, final int fromIndex) {
		final int n = _arrayList.size();

		if(fromIndex >= n) {
			return -1;
		}

		int i =  fromIndex < 0 ? 0 : fromIndex;

		if(searchValue == null) {
			for(;i<n;++i) {
				if(null == _arrayList.get(i)) {
					return i;
				}
			}
		} else {
			for(;i<n;++i) {
				if(searchValue.equals(_arrayList.get(i))) {
					return i;
				}
			}
		}
		
		return -1;
	}

	public java.lang.String join(final java.lang.Object sep) {
		final StringBuilder builder = new StringBuilder();
		final int n = _arrayList.size();
		final int m = n - 1;
		int i = 0;

		for(final java.lang.Object object : _arrayList) {
			if(null == object) {
				builder.append("null");
			} else {
				builder.append(object.toString());
			}
			
			if(i++ != m) {
				builder.append(sep);
			}
		}

		return builder.toString();
	}

	public int lastIndexOf(final java.lang.Object searchValue) {
		return _arrayList.lastIndexOf(searchValue);
	}

	public int lastIndexOf(final java.lang.Object searchValue, final int fromIndex) {
		final int n = _arrayList.size();

		int i = n - 1;

		if(fromIndex < 0) {
			return -1;
		} else if(fromIndex < i) {
			i = fromIndex;
		}

		if(null == searchValue) {
			for(; i > -1; i--) {
				if(null == _arrayList.get(i)) {
					return i;
				}
			}
		} else {
			for(; i > -1; i--) {
				if(searchValue.equals(_arrayList.get(i))) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public Array map(final Function3<java.lang.Object, Integer, Array, java.lang.Object> callback) {
		return map(callback, null);
	}

	public Array map(final Function3<java.lang.Object, Integer, Array, java.lang.Object> callback, final jitb.lang.Object thisObject) {
		final int n = _arrayList.size();
		final ArrayList<java.lang.Object> result = new ArrayList<java.lang.Object>(n);

		for(int i = 0; i < n; ++i) {
			result.set(i, callback.apply3(thisObject, _arrayList.get(i), i, this));
		}

		return new Array(result);
	}

	public Array map(final Function<java.lang.Object> callback) {
		return map((Function3<java.lang.Object, Integer, Array, java.lang.Object>)callback, null);
	}

	public Array map(final Function<java.lang.Object> callback, final jitb.lang.Object thisObject) {
		return map((Function3<java.lang.Object, Integer, Array, java.lang.Object>)callback, thisObject);
	}

	public java.lang.Object pop() {
		return _arrayList.remove(_arrayList.size() - 1);
	}

	public long push(final java.lang.Object ...args) {
		_arrayList.addAll(Arrays.asList(args));
		return _arrayList.size();
	}

	public Array reverse() {
		final Array result = concat();
		Collections.reverse(result._arrayList);
		return result;
	}

	public java.lang.Object shift() {
		return _arrayList.remove(0);
	}

	public Array slice() {
		return slice(0);
	}

	public Array slice(final int startIndex) {
		return slice(startIndex, 16777215);
	}

	public Array slice(final int startIndex, final int endIndex) {
		final int n = _arrayList.size();
		final int from = startIndex < 0 ? 0 : startIndex;
		final int to = endIndex < 0 ? n : (endIndex > n ? n : endIndex);
		final int m = to - from;

		if(0 == m) {
			return new Array(_arrayList);
		}

		final Array result = new Array(m);
		final ArrayList<java.lang.Object> list = result._arrayList;

		for(int i = from; from < to; ++i) {
			list.add(_arrayList.get(i));
		}

		return result;
	}
	
	public boolean some(final Function3<java.lang.Object, Integer, Array, Boolean> callback) {
		return some(callback, null);
	}

	public boolean some(final Function3<java.lang.Object, Integer, Array, Boolean> callback, final jitb.lang.Object thisObject) {
		final int n = _arrayList.size();

		for(int i = 0; i < n; ++i) {
			if(callback.apply3(thisObject, _arrayList.get(i), i, this)) {
				return true;
			}
		}

		return false;
	}

	public boolean some(final Function<Boolean> callback) {
		return some((Function3<java.lang.Object, Integer, Array, Boolean>)callback, null);
	}

	public boolean some(final Function<Boolean> callback, final jitb.lang.Object thisObject) {
		return some((Function3<java.lang.Object, Integer, Array, Boolean>)callback, thisObject);
	}

	public Array sort(final java.lang.Object ...args) {
		throw new MissingImplementationException("Array.sort");
	}

	public Array sortOn(final java.lang.Object fieldName, final java.lang.Object options) {
		throw new MissingImplementationException("Array.sortOn");
	}

	public Array splice(final int startIndex, final long deleteCount, final java.lang.Object ...values) {
		throw new MissingImplementationException("Array.splice");
	}

	public java.lang.String toLocaleString() {
		return toString();
	}

	@Override
	public java.lang.String toString() {
		final StringBuilder builder = new StringBuilder();
		final int n = _arrayList.size();
		final int m = n - 1;
		int i = 0;

		for(final java.lang.Object object : _arrayList) {
			if(null == object) {
				builder.append("null");
			} else {
				builder.append(object.toString());
			}

			if(i++ != m) {
				builder.append(',');
			}
		}

		return builder.toString();
	}

	public long unshift(final java.lang.Object ...args) {
		int n = args.length;

		while(--n > -1) {
			_arrayList.add(0, args[n]);
		}

		return _arrayList.size();
	}

	@Override
	public java.lang.Object JITB$getIndex(final int index) {
		return _arrayList.get(index);
	}

	@Override
	public void JITB$setIndex(final int index, final java.lang.Object value) {
		final int n = _arrayList.size();

		if(index > n) {
			final LinkedList<java.lang.Object> list = new LinkedList<java.lang.Object>();

			for(int i = n; i < index; ++i) {
				list.addFirst(null);
			}
			
			_arrayList.addAll(list);
		}

		_arrayList.add(index, value);
	}

	public List<java.lang.Object> JITB$internalArray() {
		return _arrayList;
	}
}
