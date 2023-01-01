package com.pimzero.jinterpose.action;

import java.lang.System;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.pimzero.jinterpose.Proto;
import com.pimzero.jinterpose.Evaluator;

public class LogMethodClassVisitor extends ClassVisitor {
	private Proto.Matcher_expr when;
	private Proto.Matcher.Builder current;

	public LogMethodClassVisitor(
			ClassVisitor cv,
			Proto.Matcher_expr when,
			Proto.Matcher.Builder current,
			Proto.Action.Do.DoLogMethod config) {
		super(Opcodes.ASM9, cv);

		this.when = when;
		this.current = current;
	}


	@Override
	public MethodVisitor visitMethod(
			int access,
			java.lang.String name,
			java.lang.String descriptor,
			java.lang.String signature,
			java.lang.String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

		current.setMethodname(name);

		if (Evaluator.eval(when, current.build())) {
			return new MethodVisitor(Opcodes.ASM9, mv) {
				@Override
				public void visitFieldInsn(int opcode,
						java.lang.String owner,
						java.lang.String name,
						java.lang.String descriptor) {

						System.out.println("hello");

						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System/out",
								"println", "()V");
						//mv.visitInsn(Opcodes.POP2);
					mv.visitFieldInsn(opcode, owner, name, descriptor);
				}
			};
		}

		return mv;
	}
}
