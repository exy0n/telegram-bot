CREATE TABLE steps
(
    id             	VARCHAR(256) PRIMARY KEY NOT NULL,
    text       	   	VARCHAR(8192) NOT NULL,
    buttons        	VARCHAR(8192),
    is_need_to_save	boolean,
    next			VARCHAR(256) NOT NULL,
    regexp			VARCHAR(1024)
);