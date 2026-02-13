package br.com.infox.component.quartz;

import java.io.Serializable;
import java.util.Date;

public class JobInfo implements Serializable, Comparable<JobInfo> {

	private static final long serialVersionUID = 1L;

	private String triggerName;
	private String jobName;
	private String groupName;
	private Date nextFireTime;
	private Date previousFireTime;
	private String jobExpression;
	private boolean valid;
	private String cronExpression;

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public String getJobExpression() {
		return jobExpression;
	}

	public void setJobExpression(String jobExpression) {
		this.jobExpression = jobExpression;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public int compareTo(JobInfo jobInfo) {
		if (jobExpression == null) {
			return -1;
		} else if (jobInfo.getJobExpression() == null) {
			return 1;
		} else {
			return jobExpression.compareTo(jobInfo.getJobExpression());
		}
	}

}
