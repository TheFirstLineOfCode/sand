CREATE TABLE ACCESS_CONTROL_ENTRIES(id VARCHAR(64) PRIMARY KEY, user VARCHAR(32) NOT NULL, thing_id VARCHAR(32) NOT NULL, role VARCHAR(10) NOT NULL, removed BOOLEAN DEFAULT FALSE, update_time TIMESTAMP NOT NULL);
CREATE INDEX INDEX_ACCESS_CONTROL_ENTRIES_USER ON ACCESS_CONTROL_ENTRIES (user);
CREATE INDEX INDEX_ACCESS_CONTROL_ENTRIES_THING_ID ON ACCESS_CONTROL_ENTRIES (thing_id);
CREATE TABLE RECORDED_VIDEOS(id VARCHAR(64) PRIMARY KEY, recorder_thing_id VARCHAR(64) NOT NULL, video_name VARCHAR(32) NOT NULL, video_url VARCHAR(128) NOT NULL, recording_time TIMESTAMP NOT NULL, recording_reason VARCHAR(16) NOT NULL, stored_time TIMESTAMP NOT NULL);
CREATE INDEX INDEX_RECORDED_VIDEOS_RECORDER_THING_ID ON RECORDED_VIDEOS (recorder_thing_id);