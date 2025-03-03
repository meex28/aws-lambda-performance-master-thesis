provider "aws" {
  region = "eu-central-1"

  default_tags {
    tags = {
      app = "mte"
    }
  }
}

terraform {
  backend "s3" {
    bucket = "mte-terraform-state"
    key    = "terraform.tfstate"
    region = "eu-central-1"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.82.2"
    }
  }
}
