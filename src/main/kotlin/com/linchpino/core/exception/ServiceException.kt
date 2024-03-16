package com.linchpino.core.exception

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.util.Date

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
class ServiceException {
    private val timestamp: Date = TODO()
    private val details: String=TODO()
}
