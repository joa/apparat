/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.taas.backend.jbc

import apparat.taas.backend.TaasBackend
import apparat.taas.analysis.TaasDependencyGraphBuilder
import org.objectweb.asm.{Opcodes => JOpcodes, ClassWriter => JClassWriter}
import apparat.taas.ast._
import java.io.{PrintWriter => JPrintWriter}
import org.objectweb.asm.util.{CheckClassAdapter, TraceClassVisitor => JTraceClassVisitor}

/**
 * @author Joa Ebert
 */
class JbcBackend extends TaasBackend {
	var classMap = Map.empty[String, Array[Byte]]

	override def emit(ast: TaasAST) = {
		for(nominal <- TaasDependencyGraphBuilder(ast).topsort) {
			val cw = new JClassWriter(JClassWriter.COMPUTE_FRAMES)
			val cv = new CheckClassAdapter(new JTraceClassVisitor(cw, new JPrintWriter(System.out)), true)
			cv.visit(
				JOpcodes.V1_6,
				visibilityOf(nominal) + (nominal match {
					case i: TaasInterface => JOpcodes.ACC_INTERFACE
					case _ => JOpcodes.ACC_SUPER
				}),
				toJavaName(nominal.qualifiedName),
				null,
				nominal.base match {
					case Some(base) => base match {
						case t: TaasNominalType => toJavaName(t.nominal.qualifiedName)
						case _ => error("Expected TaasNominalType, got "+base)
					}
					case None => "java/lang/Object"
				},
				null//Array.empty[String]//TODO map to interface names...
			)
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(JOpcodes.ALOAD, 0);
			mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitInsn(JOpcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			cv.visitEnd()

			classMap += nominal.qualifiedName -> cw.toByteArray()
		}

		val cl = new JbcClassLoader(classMap)
		Thread.currentThread.setContextClassLoader(cl)

		println(Class.forName("Test00", true, cl).newInstance())
	}

	private def toJavaName(qname: String) = qname.replaceAll("\\.", "\\/")
	private def visibilityOf(somethingWithNamespace: {def namespace: TaasNamespace}) = {
		somethingWithNamespace.namespace match {
			case TaasPublic => JOpcodes.ACC_PUBLIC
			case TaasInternal => JOpcodes.ACC_PUBLIC
			case TaasProtected => JOpcodes.ACC_PROTECTED
			case TaasPrivate => JOpcodes.ACC_PRIVATE
			case TaasExplicit(_) => JOpcodes.ACC_PUBLIC
		}
	}
}