module "tested_functions" {
  source = "./modules/tested_functions"
}

module "benchmarking" {
  source           = "./modules/benchmarking"
  tested_functions = module.tested_functions.functions
}
