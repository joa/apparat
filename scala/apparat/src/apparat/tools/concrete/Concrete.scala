package apparat.tools.concrete

import apparat.utils.TagContainer
import apparat.abc._
import apparat.tools.{ApparatLog, ApparatConfiguration, ApparatTool, ApparatApplication}
import java.io.{File => JFile}
import scala.collection.immutable.Stack
import scala.annotation.tailrec
import apparat.swf.{DoABC, SwfTags}

object Concrete {
	def main(args: Array[String]): Unit = ApparatApplication(new ConcreteTool, args)

	class ConcreteTool extends ApparatTool {
		var libraries = List.empty[JFile]

		var cache = Map.empty[AbcQName, AbcNominalType]

		var abcs = List.empty[Abc]

		override def name = "Concrete"

		override def help = "  -i [file0"+JFile.pathSeparatorChar+"file1"+JFile.pathSeparatorChar+"..."+JFile.pathSeparatorChar+"fileN]	Input file(s)"

		override def configure(config: ApparatConfiguration): Unit = configure(ConcreteConfigurationFactory fromConfiguration config)

		def configure(config: ConcreteConfiguration): Unit = {
			libraries = config.libraries
		}

		override def run() = {
			@tailrec def buildStack(nominalType: AbcNominalType, stack: Stack[AbcNominalType]): Stack[AbcNominalType] = {
				nominalType.inst.base match {
					case Some(base) => base match {
						case qname: AbcQName => buildStack(lookup(qname), stack push nominalType)
						case _ => error("Unexpected name " + base + ".")
					}
					case None => stack push nominalType
				}
			}

			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			abcs = libraries flatMap {
				library => {
					(TagContainer fromFile library).tags collect { case x: DoABC => x } map { Abc fromDoABC _ }
				}
			}

			for(abc <- abcs;
				nominalType <- abc.types if nominalType.inst.base.isDefined && !nominalType.inst.isInterface) {

				val stack = buildStack(nominalType, Stack.empty[AbcNominalType])
				var abstracts = List.empty[AbcQName]
				var concretes = List.empty[AbcQName]

				for(currentType <- stack; `trait` <- currentType.inst.traits) {
					`trait` match {
						case anyMethod: AbcTraitAnyMethod => {
							if(anyMethod.metadata.isDefined && (anyMethod.metadata.get exists { _.name == 'Abstract })) {
								if(concretes contains anyMethod) {
									ApparatLog.err("Error in class "+toPackage(currentType.inst.name)+
											": Method "+anyMethod.name.name+
											" has already been marked abstract.")
								}

								if(nominalType != currentType) {
									abstracts = anyMethod.name :: abstracts
								}
							} else if(abstracts contains anyMethod.name) {
								concretes = anyMethod.name :: concretes
							}
						}
						case _ =>
					}
				}

				for(error <- abstracts diff concretes) {
					ApparatLog.err("Class "+toPackage(nominalType.inst.name)+" must implement abstract method "+error.name.name)
				}
			}
		}

		@inline private def lookup(qname: AbcQName): AbcNominalType = {
			def search(qname: AbcQName): AbcNominalType = {
				for(abc <- abcs) {
					val nominal = abc.types find (_.inst.name == qname)

					if(nominal.isDefined) {
						return nominal.get
					}
				}

				error("Could not find class "+toPackage(qname))
			}

			val result = cache.get(qname)

			if(result.isDefined) {
				result.get
			} else {
				val nominal = search(qname)
				cache = cache + (qname -> nominal)
				nominal
			}
		}

		private def toPackage(qname: AbcQName) = {
			val nsString = qname.namespace.name.name

			if(nsString.isEmpty) {
				qname.name.name
			} else {
				nsString+"."+qname.name.name
			}
		}
	}
}