package com.asu.log.helpers

import java.util.UUID

/**
  * Created by hjun  chenhj on 2017/10/26.
  */

object ID {
  def gen=UUID.randomUUID().toString.replace("-", "").substring(0, 24)
}
