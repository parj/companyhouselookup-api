![CircleCI](https://img.shields.io/circleci/build/github/parj/companyhouselookup-api) [![Known Vulnerabilities](https://snyk.io/test/github/parj/companyhouselookup-api/badge.svg)](https://snyk.io/test/github/parj/companyhouselookup-api) ![GitHub](https://img.shields.io/github/license/parj/companyhouselookup-api)

## Introduction

This repository puts a web wrapper around the https://github.com/parj/companyhouselookup .

The microservice is written using [muserver.io](https://muserver.io/) and is packaged into a docker image using [Distroless](https://github.com/GoogleContainerTools/distroless) images as base. Included with this is also Kubernetes deployment files written using [KPT](https://googlecontainertools.github.io/kpt/) package manager.

## To get this running

Clone the repo. 

Set the hostname of your kubernetes cluster via [kpt](https://googlecontainertools.github.io/kpt/). Example: `kpt cfg apply k8s\ host localhost`.

Create namespace external. Example `kubectl create namespace external` .

Deploy the company house lookup api as a secret. Documentation on doing this -> https://kubernetes.io/docs/concepts/configuration/secret/ .

Example - create secret.yaml (example below), run `kubectl apply -f secret.yaml`

       apiVersion: v1
       kind: Secret
       metadata:
         name: lookup-secret
         namespace: external
       type: Opaque
       data:
         companyhouselookupapi: Randomebase64encodedsecret===
         
Warning: Do not commit the secret.yaml to github.

Finally, run `kpt live init` and then `kpt live apply k8s\` . To stop and remove everything. `kpt live destroy k8s\`.

## Help, how do I get Kubernetes on my laptop

Couple of different ways of doing this, documented here -> https://kubernetes.io/docs/setup/learning-environment/minikube/ .

My personal preference is on Linux is to use [https://microk8s.io/](https://microk8s.io/). Once installed, I enable the addons ingress, dns.

If the cluster is unable to connect to the internet -> https://microk8s.io/docs/troubleshooting. 


       sudo iptables -P FORWARD ACCEPT
       sudo apt-get install iptables-persistent
       

OR

       sudo ufw default allow routed
           

If you need to enable insecure API server, a [script](https://gist.github.com/parj/a8c66762890e18d37ed82d132778e527) needs to be run.