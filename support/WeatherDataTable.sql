-- Table: public.weatherdata

-- DROP TABLE IF EXISTS public.weatherdata;

CREATE TABLE IF NOT EXISTS public.weatherdata
(
    sample_time timestamp with time zone NOT NULL,
    outdoor_temperature real,
    outdoor_humidity real,
    wind_speed real,
    wind_direction real,
    rain_fall real,
    pressure real,
    solar real,
    lightning real,
    indoor_temperature real,
    indoor_humidity real,
    CONSTRAINT weatherdata_pkey PRIMARY KEY (sample_time)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.weatherdata
    OWNER to weather;