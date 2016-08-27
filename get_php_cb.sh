#!/bin/bash
#
# Acquires PHP codebases for major PHP projects used
# by a vast majority of websites: Joomla, Drupal, Wordpress.
#

#OPTS="--depth 100"
OPTS=""


git clone $OPTS https://github.com/joomla/joomla-cms.git    data/joomla
git clone $OPTS https://github.com/drupal/drupal.git        data/drupal
git clone $OPTS https://github.com/WordPress/WordPress.git  data/wordpress


