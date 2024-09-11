package com.linchpino.core.repository

import com.linchpino.core.entity.Schedule
import org.springframework.data.repository.CrudRepository

interface ScheduleRepository : CrudRepository<Schedule, Long>
