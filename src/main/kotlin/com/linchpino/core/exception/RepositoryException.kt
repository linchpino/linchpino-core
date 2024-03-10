package com.linchpino.core.exception

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.util.*

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
class RepositoryException {
        val timestamp: Date = TODO()
        val details: List<String> = TODO()
}