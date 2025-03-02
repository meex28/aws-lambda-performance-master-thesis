variable "tested_functions" {
  type = list(object({
    arn  = string
    name = string
  }))
  description = "List of benchmarked functions"
}
