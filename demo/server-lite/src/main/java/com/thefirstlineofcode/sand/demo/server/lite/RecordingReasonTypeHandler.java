package com.thefirstlineofcode.sand.demo.server.lite;

import org.apache.ibatis.type.EnumTypeHandler;

import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded.RecordingReason;

public class RecordingReasonTypeHandler extends EnumTypeHandler<RecordingReason> {
	public RecordingReasonTypeHandler() {
		super(RecordingReason.class);
	}
}
