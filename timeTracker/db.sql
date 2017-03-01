CREATE DATABASE IF NOT EXISTS `track_time` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `track_time`;

CREATE TABLE `time_recorded` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `time` time NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


ALTER TABLE `time_recorded`
  ADD PRIMARY KEY (`id`);