package com.pimzero.jinterpose.action;

import java.lang.System;
import java.io.*;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.pimzero.jinterpose.Evaluator;
import com.pimzero.jinterpose.Proto;

import java.awt.Color;

public class FieldNewClassVisitor extends ClassVisitor {
	private Proto.Matcher_expr when;
	private Proto.Matcher.Builder current;

	private String src_name;
	private String src_owner;

//public class UpdateFieldInsnMethodAdaptor extends MethodVisitor {
//		public UpdateFieldInsnMethodAdaptor(MethodVisitor mv) {
//			super(Opcodes.ASM9, mv);
//		}
//	};

//	public class UpdateFieldInsnClassAdaptor extends ClassVisitor {
//		public UpdateFieldInsnClassAdaptor(ClassVisitor cv) {
//			super(Opcodes.ASM9, cv);
//		}
//	}

	public FieldNewClassVisitor(
			ClassVisitor cv,
			Proto.Matcher_expr when,
			Proto.Matcher.Builder current,
			Proto.Action.Do.DoFieldNew config) {
		super(Opcodes.ASM9, cv);

		this.when = when;
		this.current = current;

		this.src_owner = config.getSrcOwner();
		this.src_name = config.getSrcName();
	}

	@Override
	public MethodVisitor visitMethod(
			int access,
			java.lang.String name,
			java.lang.String descriptor,
			java.lang.String signature,
			java.lang.String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);


		//current.setMethodname(name);

		if (Evaluator.eval(when, current.build())) {
			System.out.println("name: " + name);
			return new MethodVisitor(Opcodes.ASM9, mv) {
				@Override
				public void VisitEnd() {
					mv.visitLdcInsn(Color.blue);
					mv.VisitFieldInsn(Opcodes.PUTSTATIC,
							  src_owner,
							  src_name,
							  "java/awt/Color");

					mv.visitInsn(Opcodes.RETURN);

					mv.VisitEnd();
				}

			};
		//	switch (this.config.getOutputStream()) {
		//		case STDOUT:
		//			mv.visitFieldInsn(Opcodes.GETSTATIC,
		//					"java/lang/System",
		//					"out",
		//					"Ljava/io/PrintStream;");
		//			break;
		//		case STDERR:
		//			mv.visitFieldInsn(Opcodes.GETSTATIC,
		//					"java/lang/System",
		//					"err",
		//					"Ljava/io/PrintStream;");
		//			break;
		//		default:
		//			throw new IllegalArgumentException("Unsupported output_stream");
		//	}

		//	mv.visitLdcInsn(MessageFormat.format(LogMethodClassVisitor.this.config.getFormat(),
		//				/* 0 */ this.current.getClassname(),
		//				/* 1 */ name,
		//				/* 2 */ descriptor,
		//				/* 3 */ signature
		//				));

		//	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
		//			   "java/io/PrintStream",
		//			   "println",
		//			   "(Ljava/lang/String;)V", false);

		//	return new MethodVisitor(Opcodes.ASM9, mv) {
		//		@Override
		//		public void visitMaxs(int maxStack, int maxLocals) {
		//			mv.visitMaxs(Math.max(maxStack, 2), maxLocals);
		//		}
		//	};
		}

		return mv;
	}

	@Override
	public FieldVisitor visitField(
			int access,
			java.lang.String name,
			java.lang.String descriptor,
			java.lang.String signature,
			java.lang.Object value) {

		if (Evaluator.eval(when, current.build())) {
			return cv.visitField(access, name, descriptor, signature, value);
		} else {
			return cv.visitField(access, name, descriptor, signature, value);
		}
	}
}
