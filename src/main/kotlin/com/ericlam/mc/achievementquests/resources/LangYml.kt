package com.ericlam.mc.achievementquests.resources

import com.hypernite.mc.hnmc.core.config.yaml.MessageConfiguration
import com.hypernite.mc.hnmc.core.config.yaml.Prefix
import com.hypernite.mc.hnmc.core.config.yaml.Resource

@Prefix(path = "prefix")
@Resource(locate = "lang.yml")
class LangYml : MessageConfiguration()