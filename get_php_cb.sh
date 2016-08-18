#!/bin/bash
#
# Acquires PHP codebases for major PHP projects used
# by a vast majority of websites: Joomla, Drupal, Wordpress.
#

git clone --depth 100 https://github.com/joomla/joomla-cms.git    data/joomla
git clone --depth 100 https://github.com/drupal/drupal.git        data/drupal
git clone --depth 100 https://github.com/WordPress/WordPress.git  data/wordpress


