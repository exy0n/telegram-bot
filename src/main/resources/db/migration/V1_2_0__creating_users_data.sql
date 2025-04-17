CREATE TABLE data
(
    id        		UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dialog_id		UUID 			NOT NULL,
    username	    VARCHAR(256)	NOT NULL,
    firstname		VARCHAR(256)	NOT NULL,
    step_id        	VARCHAR(8192)	NOT NULL,
    data			VARCHAR(8192)	NOT NULL,
    created			TIMESTAMP 		DEFAULT current_timestamp
);