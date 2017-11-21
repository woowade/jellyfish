package com.ngc.seaside.jellyfish.cli.command.createjavaservicebase.dto2;

import java.util.List;

public class BasicPubSubDto {

   private String name;
   private String inputType;
   private String outputType;
   private String serviceMethod;
   private String publishMethod;
   private List<IOCorrelationDto> inputOutputCorrelations;
   
	public String getName() {
		return name;
	}
	
	public BasicPubSubDto setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getInputType() {
		return inputType;
	}
	
	public BasicPubSubDto setInputType(String inputType) {
		this.inputType = inputType;
		return this;
	}
	
	public String getOutputType() {
		return outputType;
	}
	
	public BasicPubSubDto setOutputType(String outputType) {
		this.outputType = outputType;
		return this;
	}
	
	public String getServiceMethod() {
		return serviceMethod;
	}
	
	public BasicPubSubDto setServiceMethod(String serviceMethod) {
		this.serviceMethod = serviceMethod;
		return this;
	}
	
	public String getPublishMethod() {
		return publishMethod;
	}
	
	public BasicPubSubDto setPublishMethod(String publishMethod) {
		this.publishMethod = publishMethod;
		return this;
	}
	
	public List<IOCorrelationDto> getInputOutputCorrelations() {
		return inputOutputCorrelations;
	}
	
	public BasicPubSubDto setInputOutputCorrelations(List<IOCorrelationDto> inputOutputCorrelations) {
		this.inputOutputCorrelations = inputOutputCorrelations;
		return this;
	}
   
   
}
