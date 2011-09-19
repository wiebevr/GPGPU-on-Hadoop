package cl_util;

/**
 * Interface for a buffered OpenCL operation. The implementation contains a
 * fixed-sized buffer (mostly an array) and the associated OpenCL buffer. Due to
 * performance issues the buffer should only created once at initialization or
 * resetBuffer(). Each implementation must provide a getResult() method or
 * similar.
 * 
 * @author christof
 * 
 * @param <T>
 *            Data types to be stored in the buffer or to be computed.
 */
public interface ICLBufferedOperation<T> {
	
	/**
	 * Returns internal buffer size. If buffer is full, data is copied to OCL device.
	 * @return internal buffer size
	 */
	public int getBufferSize();
	
	public int getBufferCount();

	/**
	 * Maximum items size which can be stored by the OCL device. Should be hardware and data type dependent!
	 * 
	 * @return Maximum objects which can be stored in the buffer.
	 */
	public int getMaxItemSize();
	
	public int getItemCount();
	
	public int getCurrentMaxItemSize();

	/**
	 * Current maximum buffer items. E.g. set by resetBuffer(int bufferItems);
	 * 
	 * @return Maximum objects which can be stored in the buffer.
	 */
	@ Deprecated
	public int getCurrentMaxBufferItems();

	/**
	 * Resets the counter and allocates new OCL memory. OCL memory could be resized or
	 * fit to getMaxItemsSize().
	 * 
	 * @param expectedItemSize
	 *            Minimum buffer size.
	 * @return actual allocated item size
	 */
	public int reset(int expectedItemSize);

	/**
	 * Resets the OCL memory to its maximum.
	 * * @return actual allocated item size
	 */
	public int reset();

	/**
	 * Appends a object to the buffer. If the buffer is full, a intermediate
	 * result should be computed.
	 * 
	 * @param v
	 */
	public void put(T v);

}
