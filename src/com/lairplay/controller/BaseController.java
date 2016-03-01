package com.lairplay.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController implements AmplifierController {
	final Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected final Map<String,List<Integer>> groups = new HashMap<>();
	protected final String hostname;
	protected final int maxVolume;
	protected final int minVolume;
	protected final float lineOutKnobVolume;
	protected List<Integer> activeZones;

	public BaseController(Properties config) throws IOException {
		setGroups(config);
		
		hostname = config.getProperty("controllerHostname");
		if (hostname == null) throw new RuntimeException("missing controllerHostname");
		
		String temp = config.getProperty("controllerMaxVolume");
		if (temp == null) throw new RuntimeException("missing controllerMaxVolume");
		maxVolume = Integer.parseInt(temp);
		
		temp = config.getProperty("controllerMinVolume");
		if (temp == null) throw new RuntimeException("missing controllerMinVolume");
		minVolume = Integer.parseInt(temp);
		
		temp = config.getProperty("lineOutKnobVolume");
		if (temp == null) throw new RuntimeException("missing lineOutKnobVolume");
		lineOutKnobVolume = Float.parseFloat(temp);
	}

	@Override
	public float getLineOutKnobVolume() {
		return lineOutKnobVolume;
	}
	
	/**
	 * 
	 * @param props
	 */
	private void setGroups(Properties props) {
		String[] groups = props.getProperty("groups").split(",");
		for (String group : groups) {
			String[] zones = props.getProperty(group).split(",");
			group = group.replaceAll("_", " "); // get rid of underbars			
			for (String zone : zones) {
				addToGroup(group, Integer.parseInt(zone));
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @param zone
	 */
	private void addToGroup(String name, int zone) {
		List<Integer> group = groups.get(name);
		if (group == null) {
			group = new ArrayList<Integer>();
			groups.put(name, group);
		}
		
		group.add(zone);
	}

	@Override
	public Set<String> getGroups() {
		return groups.keySet();
	}

	@Override
	public void close() throws IOException { }

	@Override
	public synchronized boolean activateGroup(String name) throws IOException {
			return true;
	}
	
	@Override
	public synchronized boolean deactivateGroup(String name) throws IOException {
		return true;
	}
	
	@Override
	public synchronized boolean setVolume(float knobVolume) throws IOException {
		return true;
	}
}
