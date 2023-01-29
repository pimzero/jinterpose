package com.pimzero.jinterpose.action;

import java.lang.System;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.pimzero.jinterpose.Evaluator;
import com.pimzero.jinterpose.Proto;

public class FieldInterpositionClassVisitor extends ClassVisitor {
	private Proto.Matcher_expr when;
	private Proto.Matcher.Builder current;

	private String src_name;
	private String src_owner;
	private String dst_name;
	private String dst_owner;

	public class UpdateFieldInsnMethodAdaptor extends MethodVisitor {
		public UpdateFieldInsnMethodAdaptor(MethodVisitor mv) {
			super(Opcodes.ASM9, mv);
		}
	};

	public class UpdateFieldInsnClassAdaptor extends ClassVisitor {
		public UpdateFieldInsnClassAdaptor(ClassVisitor cv) {
			super(Opcodes.ASM9, cv);
		}
	}

	public FieldInterpositionClassVisitor(
			ClassVisitor cv,
			Proto.Matcher_expr when,
			Proto.Matcher.Builder current,
			Proto.Action.Do.DoFieldInterposition config) {
		super(Opcodes.ASM9, cv);

		this.when = when;
		this.current = current;

		this.src_owner = config.getSrcOwner();
		this.src_name = config.getSrcName();
		this.dst_owner = config.getDstOwner();
		this.dst_name = config.getDstName();
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

					if (name.equals(src_name) &&
					    owner.equals(src_owner)) {
						owner = dst_owner;
						name = dst_name;
					}
					mv.visitFieldInsn(opcode, owner, name, descriptor);
				}
			};
		}

		return mv;
	}
}
