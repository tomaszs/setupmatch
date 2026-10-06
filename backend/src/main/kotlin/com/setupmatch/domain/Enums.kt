package com.setupmatch.domain

enum class EquipmentType {
    main_computer,
    monitor,
    keyboard,
    mouse,
}

enum class EquipmentState {
    available,
    reserved,
    assigned,
    retired,
}

enum class AllocationState {
    allocated,
    failed,
    confirmed,
    cancelled,
}
