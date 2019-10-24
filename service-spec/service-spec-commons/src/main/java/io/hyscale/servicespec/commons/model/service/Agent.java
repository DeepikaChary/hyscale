package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent {

	private String name;
	private String image;
	private Props props;
	private List<AgentVolume> volumes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Props getProps() {
		return props;
	}

	public void setProps(Props props) {
		this.props = props;
	}

	public List<AgentVolume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<AgentVolume> volumes) {
		this.volumes = volumes;
	}

}
