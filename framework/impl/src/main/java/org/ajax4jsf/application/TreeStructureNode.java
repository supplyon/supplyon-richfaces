/**
 * 
 */
package org.ajax4jsf.application;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * @author asmirnov
 *
 */
final class TreeStructureNode implements Externalizable {
	/**
	 * TODO - implement Externalizable to reduce serialized state.
	 */
	private static final long serialVersionUID = -9038742487716977911L;

	private static final String NULL_ID = "";

	private Map<String, TreeStructureNode> facets = null;

	private List<TreeStructureNode> children = null;

	private String type;

	private String id;

	public TreeStructureNode() {
	}

	public void apply(FacesContext context, UIComponent component,
			Set<String> uniqueIds) {
		type = component.getClass().getName();
		id = component.getId();
		String clientId = component.getClientId(context);
		if (!uniqueIds.add(clientId)) {
			throw new IllegalStateException("duplicate Id for a component "
					+ clientId);
		}
		Map<String, UIComponent> componentFacets = component.getFacets();
		for (Iterator<Entry<String,UIComponent>> i = componentFacets.entrySet().iterator(); i
				.hasNext();) {
			Entry<String,UIComponent> element = i.next();
			UIComponent f = element.getValue();
			if (!f.isTransient()) {
				TreeStructureNode facet = new TreeStructureNode();
				facet.apply(context, f, uniqueIds);
				if (null == facets) {
					facets = new HashMap<String, TreeStructureNode>();
				}
				facets.put(element.getKey(), facet);

			}
		}
		for (Iterator<UIComponent> i = component.getChildren().iterator(); i.hasNext();) {
			UIComponent child = i.next();
			if (!child.isTransient()) {
				TreeStructureNode t = new TreeStructureNode();
				t.apply(context, child, uniqueIds);
				if (null == children) {
					children = new ArrayList<TreeStructureNode>();
				}
				children.add(t);

			}
		}
	}

	public UIComponent restore(ComponentsLoader loader) {
		UIComponent component;
		component = loader.createComponent(type);
		component.setId(id);
		if (null != facets) {
			for (Iterator<Entry<String, TreeStructureNode>> i = facets.entrySet().iterator(); i.hasNext();) {
				Entry<String, TreeStructureNode> element =  i.next();
				UIComponent facet = ( element.getValue())
						.restore(loader);
				component.getFacets().put(element.getKey(), facet);
			}

		}
		if (null != children) {
			for (Iterator<TreeStructureNode> i = children.iterator(); i.hasNext();) {
				TreeStructureNode node = i.next();
				UIComponent child = node.restore(loader);
				component.getChildren().add(child);
			}

		}
		return component;
	}

	/**
	 * @return the facets
	 */
	public Map<String, TreeStructureNode> getFacets() {
		return facets;
	}

	/**
	 * @param facets
	 *            the facets to set
	 */
	public void setFacets(Map<String, TreeStructureNode> facets) {
		this.facets = facets;
	}

	/**
	 * @return the children
	 */
	public List<TreeStructureNode> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<TreeStructureNode> children) {
		this.children = children;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		type = in.readUTF();
		id = in.readUTF();
		if (NULL_ID.equals(id)) {
			id = null;
		}
		int facetsSize = in.readInt();
		if (facetsSize > 0) {
			facets = new HashMap<String, TreeStructureNode>(facetsSize);
			for (int i = 0; i < facetsSize; i++) {
				String facetName = in.readUTF();
				TreeStructureNode facet = new TreeStructureNode();
				facet.readExternal(in);
				facets.put(facetName, facet);
			}
		}
		int childrenSize = in.readInt();
		if (childrenSize > 0) {
			children = new ArrayList<TreeStructureNode>(childrenSize);
			for (int i = 0; i < childrenSize; i++) {
				TreeStructureNode child = new TreeStructureNode();
				child.readExternal(in);
				children.add(child);
			}
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(type);
		out.writeUTF(null == id ? NULL_ID : id);
		if (null != facets) {
			out.writeInt(facets.size());
			for (Iterator<Map.Entry<String, TreeStructureNode>> i = facets.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, TreeStructureNode> entry = i.next();
				out.writeUTF(entry.getKey());
				TreeStructureNode node = entry.getValue();
				node.writeExternal(out);
			}

		} else {
			out.writeInt(0);
		}
		if (null != children) {
			out.writeInt(children.size());
			for (Iterator<TreeStructureNode> i = children.iterator(); i.hasNext();) {
				TreeStructureNode child = i.next();
				child.writeExternal(out);
			}

		} else {
			out.writeInt(0);
		}
	}
}