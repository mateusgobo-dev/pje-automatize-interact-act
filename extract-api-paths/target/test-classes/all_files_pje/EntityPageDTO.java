package br.jus.pje.nucleo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityPageDTO<E> {
	
	private Boolean first;
	private Boolean last;
	private Integer number;
	private Integer numberOfElements;
	private Integer size;
	private Sort	sort;
	private Integer totalElements;
	private Integer totalPages;
	private List<E> content;
	
	public EntityPageDTO() {
		super();
	}

	public EntityPageDTO(Boolean first, Boolean last, Integer number, Integer numberOfElements, Integer size,
			Sort sort, Integer totalElements, Integer totalPages) {
		super();
		this.first = first;
		this.last = last;
		this.number = number;
		this.numberOfElements = numberOfElements;
		this.size = size;
		this.sort = sort;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
	}

	public Boolean getFirst() {
		return first;
	}
	
	public void setFirst(Boolean first) {
		this.first = first;
	}
	
	public Boolean getLast() {
		return last;
	}
	
	public void setLast(Boolean last) {
		this.last = last;
	}
	
	public Integer getNumber() {
		return number;
	}
	
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public Integer getNumberOfElements() {
		return numberOfElements;
	}
	
	public void setNumberOfElements(Integer numberOfElements) {
		this.numberOfElements = numberOfElements;
	}
	
	public Integer getSize() {
		return size;
	}
	
	public void setSize(Integer size) {
		this.size = size;
	}
	
	public Sort getSort() {
		return sort;
	}
	
	public void setSort(Sort sort) {
		this.sort = sort;
	}
	
	public Integer getTotalElements() {
		return totalElements;
	}
	
	public void setTotalElements(Integer totalElements) {
		this.totalElements = totalElements;
	}
	
	public Integer getTotalPages() {
		return totalPages;
	}
	
	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}
	
	public List<E> getContent() {
		return content;
	}
	
	public void setContent(List<E> content) {
		this.content = content;
	}

	private class Sort {
		
		private Boolean sorted;
		private Boolean unsorted;
		
		public Sort(Boolean sorted, Boolean unsorted) {
			super();
			this.sorted = sorted;
			this.unsorted = unsorted;
		}
		
		public Sort() {
			super();
		}
		
		public Boolean getSorted() {
			return sorted;
		}
		public void setSorted(Boolean sorted) {
			this.sorted = sorted;
		}

		public Boolean getUnsorted() {
			return unsorted;
		}

		public void setUnsorted(Boolean unsorted) {
			this.unsorted = unsorted;
		}
	}

}