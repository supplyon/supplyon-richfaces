/**
 * 
 */
package org.ajax4jsf.event;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.faces.event.FacesEvent;

/**
 * Very simple implementation of FIFO buffer, to organize JSF events queue.
 * @author asmirnov
 *
 */
public class EventsQueue  extends AbstractQueue<FacesEvent>{
	
	private QueueElement first;
	
	private QueueElement last;

	private int size = 0;
	
	/**
	 * Remove and return first queued event.
	 * @return faces event form top of queue
	 * @throws NoSuchElementException , if queue is empty.
	 */
	public FacesEvent poll()  {
		FacesEvent element = null;
		if (!isEmpty()) {
			element = first.getElement();
			first = first.getPrevious();
			if (null == first) {
				last = null;
				size = 0;
			} else {
			size--;
			}
		}
		return element;
	}

	public FacesEvent peek()  {
		FacesEvent element = null;
		if (!isEmpty()) {
			element = first.getElement();
		}
		return element;
	}
	/**
	 * Add event to queue.
	 * @param element
	 * @return TODO
	 */
	public boolean offer(FacesEvent element) {
		QueueElement queueElement = new QueueElement(element);
		if(isEmpty()){
			first = last = queueElement;
		} else {
			last.setPrevious(queueElement);
			last = queueElement;
		}
		size++;
		return true;
	}

	public void clear() {
		size = 0;
		first = last = null;
	}

	public boolean isEmpty() {		
		return null == first;
	}
	
	public int size() {
		return size;
	}

	private static class QueueElement {
		
		private QueueElement previous;
		
		private FacesEvent element;

		public QueueElement(FacesEvent element) {
			this.element = element;
		}

		
		/**
		 * @param previous the previous to set
		 */
		public void setPrevious(QueueElement previsious) {
			this.previous = previsious;
		}


		/**
		 * @return the previous
		 */
		public QueueElement getPrevious() {
			return previous;
		}

		/**
		 * @return the element
		 */
		public FacesEvent getElement() {
			return element;
		}
		
	}

	@Override
	public Iterator<FacesEvent> iterator() {
		
		return new Iterator<FacesEvent>(){
			
			private QueueElement current = first;

			public boolean hasNext() {
				return null != current;
			}

			public FacesEvent next() {
				if(null == current){
					throw new NoSuchElementException();
				}
				FacesEvent event = current.getElement();
				current = current.getPrevious();
				return event;
			}

			public void remove() {
				throw new UnsupportedOperationException();				
			}
			
		};
	}
}
