package org.maidroid.reminder2

object RFC2445Repeat {

    val choose = Array(
        None,
        Some("RRULE:FREQ=DAILY"),
        Some("RRULE:FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"),
        Some("RRULE:FREQ=WEEKLY"),
        Some("RRULE:FREQ=MONTHLY")
    )

    def apply(n: Int) = choose(n)
    def apply(rfc2445String: String) = rfc2445String match {
        case "RRULE:FREQ=DAILY" => 1
        case "RRULE:FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR" => 2
        case "RRULE:FREQ=WEEKLY" => 3
        case "RRULE:FREQ=MONTHLY" => 4
    }
}

