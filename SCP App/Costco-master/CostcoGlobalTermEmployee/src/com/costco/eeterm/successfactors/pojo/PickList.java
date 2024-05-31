package com.costco.eeterm.successfactors.pojo;

/**
 * This POJO is for Legacy Picklist entity
 */
public class PickList implements Comparable<PickList> {

		private String pickListId;
		private String externalCode;
		private String label;
		private String status;
		private String uri;
		
		public String getPickListId() {
			return pickListId;
		}
		public void setPickListId(String pickListId) {
			this.pickListId = pickListId;
		}

		public String getExternalCode() {
			return externalCode;
		}
		public void setExternalCode(String externalCode) {
			this.externalCode = externalCode;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getUri() {
			return uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		@Override
		public int compareTo(PickList picklist) {
			return this.label == null ? -1 : this.label.compareToIgnoreCase(picklist.getLabel());
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((externalCode == null) ? 0 : externalCode.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + ((pickListId == null) ? 0 : pickListId.hashCode());
			result = prime * result + ((status == null) ? 0 : status.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PickList other = (PickList) obj;
			if (externalCode == null) {
				if (other.externalCode != null)
					return false;
			} else if (!externalCode.equals(other.externalCode))
				return false;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (pickListId == null) {
				if (other.pickListId != null)
					return false;
			} else if (!pickListId.equals(other.pickListId))
				return false;
			if (status == null) {
				if (other.status != null)
					return false;
			} else if (!status.equals(other.status))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}
	
}
