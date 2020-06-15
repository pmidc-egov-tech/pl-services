package org.egov.pl.workflow;

import static org.egov.pl.util.PLConstants.ACTION_APPLY;
import static org.egov.pl.util.PLConstants.ACTION_APPROVE;
import static org.egov.pl.util.PLConstants.ACTION_CANCEL;
import static org.egov.pl.util.PLConstants.ACTION_INITIATE;
import static org.egov.pl.util.PLConstants.ACTION_REJECT;
import static org.egov.pl.util.PLConstants.STATUS_APPLIED;
import static org.egov.pl.util.PLConstants.STATUS_APPROVED;
import static org.egov.pl.util.PLConstants.STATUS_CANCELLED;
import static org.egov.pl.util.PLConstants.STATUS_INITIATED;
import static org.egov.pl.util.PLConstants.STATUS_PAID;
import static org.egov.pl.util.PLConstants.STATUS_REJECTED;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:workflow.properties")
public class WorkflowConfig {

	private Environment env;

	@Autowired
	public WorkflowConfig(Environment env) {
		this.env = env;
		setActionStatusMap();
		setRoleActionMap();
		setActionCurrentStatusMap();
	}

	private String CONFIG_ROLES = "egov.workflow.pl.roles";

	private Map<String, String> actionStatusMap;

	private Map<String, List<String>> roleActionMap;

	private Map<String, List<String>> actionCurrentStatusMap;

	private void setActionStatusMap() {

		Map<String, String> map = new HashMap<>();

		map.put(ACTION_INITIATE, STATUS_INITIATED);
		map.put(ACTION_APPLY, STATUS_APPLIED);
		map.put(ACTION_APPROVE, STATUS_APPROVED);
		map.put(ACTION_REJECT, STATUS_REJECTED);
		map.put(ACTION_CANCEL, STATUS_CANCELLED);

		actionStatusMap = Collections.unmodifiableMap(map);
	}

	/*
	 * private void setRoleActionMap(){
	 * 
	 * Map<String, List<String>> map = new HashMap<>();
	 * 
	 * map.put(config.getROLE_CITIZEN(), Arrays.asList(ACTION_APPLY,
	 * ACTION_INITIATE)); map.put(config.getROLE_EMPLOYEE(),
	 * Arrays.asList(ACTION_APPLY, ACTION_INITIATE,ACTION_APPROVE,
	 * ACTION_REJECT,ACTION_CANCEL));
	 * 
	 * roleActionMap = Collections.unmodifiableMap(map); }
	 */

	private void setRoleActionMap() {

		Map<String, List<String>> map = new HashMap<>();

		String[] keys = env.getProperty(CONFIG_ROLES).split(",");

		for (String key : keys) {
			map.put(env.getProperty(key), Arrays.asList(env.getProperty(key.replace("role", "action")).split(",")));
		}

		roleActionMap = Collections.unmodifiableMap(map);
	}

	private void setActionCurrentStatusMap() {

		Map<String, List<String>> map = new HashMap<>();

		map.put(null, Arrays.asList(ACTION_APPLY, ACTION_INITIATE));
		map.put(STATUS_INITIATED, Arrays.asList(ACTION_APPLY, ACTION_INITIATE));
		map.put(STATUS_APPLIED, Arrays.asList(ACTION_APPLY)); // FIXME PUT THE ACTIONS IN PLACE
		map.put(STATUS_PAID, Arrays.asList(ACTION_APPROVE, ACTION_REJECT));
		map.put(STATUS_APPROVED, Arrays.asList(ACTION_CANCEL));
		map.put(STATUS_REJECTED, Arrays.asList()); // FIXME PUT THE ACTIONS IN PLACE
		map.put(STATUS_CANCELLED, Arrays.asList()); // FIXME PUT THE ACTIONS IN PLACE

		actionCurrentStatusMap = Collections.unmodifiableMap(map);
	}

	public Map<String, String> getActionStatusMap() {
		return actionStatusMap;
	}

	public Map<String, List<String>> getActionCurrentStatusMap() {
		return actionCurrentStatusMap;
	}

	public Map<String, List<String>> getRoleActionMap() {
		return roleActionMap;
	}

}
