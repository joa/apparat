/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.memory.strategies 
{
	import com.joa_ebert.apparat.memory.MemoryMath;
	import com.joa_ebert.apparat.memory.MemoryBlock;
	import com.joa_ebert.apparat.memory.IMemoryPoolStrategy;

	import flash.errors.MemoryError;
	import flash.utils.ByteArray;

	/**
	 * The StaticChunksStrategy class is an IMemoryPoolStrategy implementation.
	 * 
	 * <p>The StaticChunksStrategy divides the amount of available memory into
	 * chunks with the same size. It has an internal list of free chunks and
	 * tries to find chunks in the same region that fullfill the amount of 
	 * required memory.</p>
	 * 
	 * <p>The StaticChunksStrategy is pretty stupid and does nothing in order
	 * to prevent the memory from defragmenting quickly. A more convinient 
	 * approach would be to keep in interval tree of the free chunks and then
	 * using always the smalles possible interval.</p>
	 * 
	 * @author Joa Ebert
	 */
	public final class StaticChunksStrategy implements IMemoryPoolStrategy 
	{
		/**
		 * The size of a chunk in bytes.
		 */
		private var _chunkSize: uint;
		
		/**
		 * The most significant bit of the chunk size.
		 */
		private var _msb: uint;
		
		/**
		 * The buffer of the MemoryPool.
		 */
		private var _buffer: ByteArray;
		
		/**
		 * The free-list of chunks.
		 */
		private var _freeChunks: MemoryChunk;
		
		/**
		 * Creates a new StaticChunksStrategy object.
		 * 
		 * @param length The length of one chunk; defaults to 1024b. 
		 */
		public function StaticChunksStrategy( length: uint = 0x400 )
		{
			_chunkSize = MemoryMath.isPow2( length ) ? length : 
				MemoryMath.nextPow2( length );
			
			_msb  = MemoryMath.msb( _chunkSize );
		}
		
		/**
		 * @inheritDoc
		 */
		public function initialize( buffer : ByteArray ) : void
		{
			_buffer = buffer;
			
			if( !MemoryMath.isPow2( _buffer.length ) )
			{
				throw new MemoryError( 'Buffer size must be a power of two.' );
			}
		
			var numChunks: uint = _buffer.length >>> _msb;	
			var lastChunk: MemoryChunk = null;
			var currentChunk: MemoryChunk = null;
			var position: int = _chunkSize;
			
			//
			// Start from 1 since the first chunk is non-persisten space. 
			//
			
			for( var i: int = 1; i < numChunks; ++i )
			{
				currentChunk = MemoryChunk.create();
				
				currentChunk.position = position;
				currentChunk.length = _chunkSize;
				currentChunk.prev = lastChunk;
				
				position += _chunkSize;
				
				if( 1 == i )
				{
					lastChunk = _freeChunks = currentChunk;
				}
				else
				{
					lastChunk = lastChunk.next = currentChunk;
				}
			}
		}
		
		/**
		 * @inheritDoc
		 */
		public function allocate( length : uint ) : MemoryBlock
		{
			if( null == _freeChunks )
			{
				throw new MemoryError();
			}			

			var numChunks: uint = 0;
			
			if( length < _chunkSize )
			{
				numChunks = 1;
			}
			else
			{
				numChunks = length >>> _msb;
				
				if( !MemoryMath.isPow2( length ) )
					++numChunks;
			} 

			var currentChunk: MemoryChunk = _freeChunks.next;
			var lastChunk: MemoryChunk = _freeChunks;
			
			var startChunk: MemoryChunk = lastChunk;
			var endChunk: MemoryChunk = lastChunk;
			
			var availableChunks: int = 1;
			
			if( numChunks != 1 )
			{
				while( null != currentChunk )
				{
					if( currentChunk.position == ( lastChunk.position + 
						lastChunk.length ) )
					{
						if( ++availableChunks == numChunks )
						{
							endChunk = currentChunk;
							break;
						} 
					}
					else
					{
						startChunk = currentChunk;
						endChunk = null;
						
						availableChunks = 1;
					}
					
					lastChunk = currentChunk;
					currentChunk = currentChunk.next;
				}
				
				if( availableChunks != numChunks )
				{
					//
					// Could defragment here. This will take too long. We are
					// actually harsh and throw the error.
					//
					
					throw new MemoryError();
				}
				
				return createMemoryBlock( startChunk, endChunk, numChunks );
			}
			else
			{
				return createMemoryBlock( startChunk, endChunk, numChunks );	
			}
		}

		/**
		 * Creates and returns a MemoryBlock object for a list of chunks.
		 * The chunks will be spliced from the free-list.
		 * 
		 * @param endChunk The last chunk to splice.
		 * @param numChunks The number of chunks used.
		 * @param startChunk The first chunk to splice.
		 * 
		 * @return A MemoryBlock object representing the given chunks.
		 */
		private function createMemoryBlock( startChunk: MemoryChunk, 
			endChunk: MemoryChunk, numChunks: uint ): MemoryBlock
		{
			const memoryBlock: MemoryBlock = MemoryBlock.create(
				startChunk.position, numChunks << _msb );
			
			var spliceFrom: MemoryChunk = startChunk.prev;
			var spliceTo: MemoryChunk = endChunk.next;
			
			if( null == spliceFrom )
			{
				if( null == spliceTo )
				{
					_freeChunks = null;
				}
				else
				{
					_freeChunks = spliceTo;
					spliceTo.prev = null;
				}
			}
			else
			{
				if( null == spliceTo )
				{
					spliceFrom.next = null;
				}
				else
				{
					spliceFrom.next = spliceTo;
					spliceTo.prev = spliceFrom;
				}
			}
			
			var chunk: MemoryChunk = startChunk;
			var nextChunk: MemoryChunk;
			
			while( true )
			{
				nextChunk = chunk.next;

				chunk.dispose();
				
				if( chunk == endChunk )
				{
					break;
				}
				
				chunk = nextChunk;
			}
			 			
			return memoryBlock;
		}
		
		/**
		 * @inheritDoc
		 */
		public function free( block : MemoryBlock ) : void
		{
			const numChunks: int = block.length >>> _msb;
			
			if( numChunks < 1 )
			{
				throw new Error( 'Corrupted MemoryBlock.' );
			}
			
			var currentChunk: MemoryChunk = null;
			var firstChunk: MemoryChunk = null;
			var lastChunk: MemoryChunk = null;
			var position: int = block.position;
			
			for( var i: int = 0; i < numChunks; ++i )
			{
				currentChunk = MemoryChunk.create();
				currentChunk.position = position;
				currentChunk.length = _chunkSize;
				currentChunk.prev = lastChunk;
				
				position += _chunkSize;
				
				if( 0 == i )
				{
					lastChunk = firstChunk = currentChunk;
				}
				else
				{
					lastChunk = lastChunk.next = currentChunk;
				}
			}
			
			if( null == _freeChunks )
			{
				_freeChunks = firstChunk;
			}
			else
			{
				if( _freeChunks.position > currentChunk.position ) 
				{
					lastChunk.next = _freeChunks;
					_freeChunks.prev = lastChunk;
					_freeChunks = currentChunk;
				}
				else
				{
					currentChunk = _freeChunks;
					
					while( true )
					{
						if( currentChunk.position < firstChunk.position )
						{
							if( null == currentChunk.next
								|| currentChunk.next.position > firstChunk.position )
							{
								break;
							}
							
							currentChunk = currentChunk.next;
						}
						else
						{
							throw new Error( 'Unreachable by definition.' );	
						}
					}
					
					firstChunk.prev = currentChunk;
					currentChunk.next.prev = lastChunk;
					lastChunk.next = currentChunk.next;
				}
			}
		}
	}
}
